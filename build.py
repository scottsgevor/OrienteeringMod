"""Rebuild the recovered Forge 1.12.2 mod on Windows with the installed Java 8."""
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parent
MC = Path(os.environ.get("MC_HOME", Path(os.environ["APPDATA"]) / ".minecraft"))
PROFILE = MC / "versions/Forge 1.12.2/Forge 1.12.2.json"
CACHE = Path(os.environ["LOCALAPPDATA"]) / "OrienteeringMod-build/1.12.2-2861"
JAVA = Path(os.environ.get("JAVA8_HOME", MC / "runtime/jre-legacy/windows/jre-legacy")) / "bin/java.exe"
SRC = ROOT / "src/main/java"
RES = ROOT / "src/main/resources"

def game_libraries(profile):
    for library in profile["libraries"]:
        group, artifact, version = library["name"].split(":")[:3]
        jar = MC / "libraries" / group.replace(".", "/") / artifact / version / (artifact + "-" + version + ".jar")
        if group != "net.minecraftforge" and jar.is_file():
            yield jar

def main():
    profile = json.loads(PROFILE.read_text(encoding="utf-8-sig"))
    if "net.minecraftforge:forge:1.12.2-14.23.5.2861" not in [lib["name"] for lib in profile["libraries"]]:
        raise SystemExit("Expected Minecraft 1.12.2 with Forge 14.23.5.2861.")
    if not all((CACHE / n).is_file() for n in ("forge-srg.jar", "ecj.jar", "jsr305.jar")):
        subprocess.run([sys.executable, str(ROOT / "prepare.py")], check=True)
    cp = os.pathsep.join(map(str, [CACHE / "forge-srg.jar", CACHE / "jsr305.jar", *game_libraries(profile)]))
    dest = ROOT / "build/OrienteeringMod-4.6.jar"
    dest.parent.mkdir(exist_ok=True)
    with tempfile.TemporaryDirectory(dir=dest.parent) as folder:
        classes = Path(folder) / "classes"
        classes.mkdir()
        subprocess.run([str(JAVA), "-jar", str(CACHE / "ecj.jar"), "-1.8", "-proc:none",
                        "-encoding", "UTF-8", "-g", "-warn:none", "-classpath", cp,
                        "-d", str(classes), str(SRC)], check=True)
        tmp = Path(folder) / "mod.jar"
        with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as jar:
            for base in (RES, classes):
                for p in sorted(base.rglob("*")):
                    if p.is_file() and p.name not in ("fml_cache_annotation.json", "fml_cache_class_versions.json"):
                        jar.write(p, p.relative_to(base).as_posix())
        with zipfile.ZipFile(tmp) as jar:
            assert jar.testzip() is None
            assert "net/respawner/orienteeringmod/OrienteeringMod.class" in jar.namelist()
        tmp.replace(dest)
    print(dest)
    print("SHA-256:", hashlib.sha256(dest.read_bytes()).hexdigest())

if __name__ == "__main__":
    main()

