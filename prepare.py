"""Prepare compile-only, SRG-named game classes from the installed Forge profile."""
import hashlib
import io
import json
import lzma
import subprocess
import tempfile
import urllib.request
import zipfile
import zlib
from pathlib import Path
from build import CACHE, JAVA, MC, PROFILE

MAVEN = "https://repo.maven.apache.org/maven2/"
TOOLS = {
    "ss.jar": ("net/md-5/SpecialSource/1.11.4/SpecialSource-1.11.4-shaded.jar", "e2cab24b1c12400ad73b15972bb21e4273a0dc7081c8b3c136ddfdd824c78518"),
    "ecj.jar": ("org/eclipse/jdt/ecj/3.26.0/ecj-3.26.0.jar", "ac0ba5876eaf7ebb47749a0d1be179c51f194b9dd0b875d1c09e1b530f5a2db5"),
    "jsr305.jar": ("com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar", "766ad2a0783f2687962c8ad74ceecc38a28b9f72a2d085ee438b7813e928d0c7"),
}

def fetch(path, url, algorithm, expected):
    def valid(file):
        return file.exists() and hashlib.new(algorithm, file.read_bytes()).hexdigest() == expected
    if valid(path):
        return
    with tempfile.TemporaryDirectory(dir=CACHE) as folder:
        tmp = Path(folder) / path.name
        urllib.request.urlretrieve(url, tmp)
        if not valid(tmp):
            raise ValueError("Download checksum mismatch: " + url)
        tmp.replace(path)

def read_number(stream, size):
    data = stream.read(size)
    if len(data) != size:
        raise EOFError("Incomplete binpatch")
    return int.from_bytes(data, "big")

def apply_delta(original, payload):
    stream = io.BytesIO(payload)
    if stream.read(5) != b"\xd1\xff\xd1\xff\x04":
        raise ValueError("Invalid GDIFF header")
    sizes = [(2, 1), (2, 2), (2, 4), (4, 1), (4, 2), (4, 4), (8, 4)]
    result = bytearray()
    while True:
        cmd = read_number(stream, 1)
        if cmd == 0:
            return result
        if cmd < 249:
            size = cmd if cmd <= 246 else read_number(stream, 2 if cmd == 247 else 4)
            result.extend(stream.read(size))
        else:
            a, b = sizes[cmd - 249]
            start = read_number(stream, a)
            length = read_number(stream, b)
            result.extend(original[start:start + length])

def apply_client_patches(classes, patches):
    with zipfile.ZipFile(patches) as archive:
        for name in archive.namelist():
            if not name.startswith("binpatch/client/") or not name.endswith(".binpatch"):
                continue
            stream = io.BytesIO(archive.read(name))
            fields = [stream.read(read_number(stream, 2)).decode() for _ in range(3)]
            key = fields[1].replace(".", "/") + ".class"
            original = classes.get(key, b"")
            if read_number(stream, 1) and read_number(stream, 4) != zlib.adler32(original):
                raise ValueError("Installed client differs from Forge patch input: " + key)
            classes[key] = apply_delta(original, stream.read(read_number(stream, 4)))

def prepare(folder):
    profile = json.loads(PROFILE.read_text(encoding="utf-8-sig"))
    server = profile["downloads"]["server"]
    fetch(CACHE / "server.jar", server["url"], "sha1", server["sha1"])
    for file, (path, digest) in TOOLS.items():
        fetch(CACHE / file, MAVEN + path, "sha256", digest)
    forge_version = "1.12.2-14.23.5.2861"
    forge = MC / "libraries/net/minecraftforge/forge" / forge_version / ("forge-" + forge_version + ".jar")
    with zipfile.ZipFile(PROFILE.with_suffix(".jar")) as client:
        classes = {n: client.read(n) for n in client.namelist() if n.endswith(".class")}
    with zipfile.ZipFile(forge) as archive:
        mappings = lzma.decompress(archive.read("deobfuscation_data-1.12.2.lzma"))
        (folder / "joined.srg").write_bytes(mappings)
        (folder / "patch.pack").write_bytes(lzma.decompress(archive.read("binpatches.pack.lzma")))
        subprocess.run([str(JAVA.with_name("unpack200.exe")), "patch.pack", "patch.zip"], cwd=folder, check=True)
        apply_client_patches(classes, folder / "patch.zip")
        with zipfile.ZipFile(CACHE / "server.jar") as dedicated:
            for line in mappings.decode().splitlines():
                if line.startswith("CL: "):
                    name = line.split()[1] + ".class"
                    if name not in classes and name in dedicated.NameToInfo:
                        classes[name] = dedicated.read(name)
        for n in archive.namelist():
            if n.endswith(".class"):
                classes[n] = archive.read(n)
    with zipfile.ZipFile(folder / "merged.jar", "w", zipfile.ZIP_DEFLATED) as merged:
        for name, data in classes.items():
            merged.writestr(name, data)
    subprocess.run([str(JAVA), "-jar", str(CACHE / "ss.jar"), "-i", "merged.jar",
                    "-o", "forge-srg.jar", "-m", "joined.srg", "--quiet"], cwd=folder, check=True)
    (folder / "forge-srg.jar").replace(CACHE / "forge-srg.jar")

if __name__ == "__main__":
    CACHE.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(dir=CACHE) as work:
        prepare(Path(work))

