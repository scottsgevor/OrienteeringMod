# OrienteeringMod 4.7 使用说明

> 本文档对应 OrienteeringMod 1.12.2 4.7。Word 版仅保留作旧格式参考；配置字段、命令和代码示例以本 Markdown 为准。

## 1. 文件管理

比赛数据保存在游戏根目录的 `.orienteering` 文件夹中：

```text
.orienteering/
├─ <比赛名>_<玩家名>.txt          成绩条
├─ <比赛名>_track.png             轨迹图
└─ <比赛名>/
   ├─ players/
   │  └─ <玩家名>                 玩家打点/比赛记录
   ├─ courses/
   │  └─ <路线名>                 路线数据
   ├─ maps/                       地图领取台发放的图片（PNG/JPG/JPEG）
   ├─ grades/                     成绩缓存
   ├─ match                       已生成的比赛对象
   ├─ matchseed.json              比赛源文件
   ├─ roster.json                 选手名册
   ├─ 0_stat.txt                  成统总表
   ├─ 1_statdetail.txt            成统明细（含 back 记录）
   ├─ cartography.json            制图配置
   ├─ ground.png                  制图输出
   ├─ foliage.png                 树冠与等高线
   ├─ ground-height.png           地面高程分层设色图
   └─ ground-height.csv           逐格地面高度
```

说明：

- 每个比赛文件夹包含完整的比赛数据，可以整体转移到其他服务器。
- 服务器运行时，比赛数据、成统文件和 `match` 数据在服务端；轨迹记录和 `_track.png` 输出在客户端对应的游戏目录，轨迹记录位于客户端 `config/orienteering`。
- 玩家每次开始新比赛都会清空该玩家旧比赛记录，且没有自动备份。
- 玩家 ID 区分大小写，但不同玩家 ID 会被视为不同参赛者。

## 2. 比赛生成

比赛的源文件是 `matchseed.json` 和可选的 `roster.json`。

- 单机：在世界名与比赛名相同的情况下输入 `/ORT generate`。
- 专用服务器：输入 `/ORT generate [比赛名]`，生成后会直接加载该比赛。
- 单机省略比赛名时默认使用当前世界名。
- `matchseed.json` 的 `version` 必须为 `4`。

> **重要：** 修改 `matchseed.json` 后，需要重新生成比赛或新建比赛才会生效。仅重启服务器或执行 `/ORT loadMatch` 不会重新读取 `matchseed.json`，因为已生成比赛保存在 `.orienteering/<比赛名>/match` 中。重新生成会清空旧比赛数据。

生成成功后，按正常流程布置检查点、起点、终点、赛事中心，再进行比赛。

## 3. matchseed.json

一个完整的单场比赛示例：

```json
{
  "version": 4,
  "matchtype": "single",
  "matchname": "example-match",
  "useplayerroster": false,
  "announcestart": false,
  "defaultcourse": "course1",
  "deathpolicy": "penalty",
  "deathpenalty": 0,
  "backpenalty": 5,
  "spawnradius": 3,
  "drawcompass": true,
  "nightvision": true,
  "customfalldamage": true,
  "spawn": [0, 64, 0],
  "admin": [],
  "spectator": [],
  "worldmode": "admin",
  "courses": [
    {
      "name": "course1",
      "cplist": [31, 32],
      "coursetype": "classic",
      "validtime": 20,
      "start": [0, 64, 0],
      "needteleport": false,
      "welcome": "Welcome to the course."
    }
  ]
}
```

### 3.1 比赛级字段

| 字段 | 类型 | 说明 |
|---|---|---|
| `version` | int | matchseed 格式版本，当前必须填 `4`。 |
| `matchtype` | string | `single`、`relay`、`team`。`relay`/`team` 必须使用花名册。 |
| `matchname` | string | 比赛名称。单机默认应与世界名相同；专用服务器可以用 `/ORT generate [比赛名]` 指定。 |
| `useplayerroster` | boolean | 是否使用 `roster.json`。启用后名册相当于白名单；管理员不受限制。 |
| `announcestart` | boolean | 是否需要管理员用 `/ORT openMatch` 和 `/ORT closeMatch` 手动开关比赛。 |
| `defaultcourse` | string | 没有通过名册分配路线时使用的默认路线。 |
| `deathpolicy` | string | `penalty` 或 `abort`。`penalty`：扣时并在出生半径内重生；`abort`：死亡后直接结束比赛并 DSQ。 |
| `deathpenalty` | int | 死亡扣时，单位分钟（1 点 = 1 分钟）。仅 `penalty` 使用。 |
| `backpenalty` | int | `/ORT back` 的扣时，单位分钟，省略时默认 `5`；`0` 表示不罚时。计入最终成绩。 |
| `spawnradius` | int | 死亡后随机重生的半径。仅 `penalty` 使用。 |
| `drawcompass` | boolean | 是否显示指北针。 |
| `nightvision` | boolean | 是否给予夜视效果。 |
| `customfalldamage` | boolean | 是否使用 ORT 特殊摔落规则，默认 `true`，详见 3.4。 |
| `spawn` | int[3] | 赛事中心的 `[x, y, z]`，完成比赛或死亡后回到这里。 |
| `admin` | string[] | 管理员名单。列出的玩家拥有 ORT 管理权限；在线原版 OP 也视为 ORT 管理员。ORT 管理员不会自动获得原版 OP。 |
| `spectator` | string[] | 旁观者名单，进入后为旁观模式。不能与管理员名单重合。 |
| `worldmode` | string | `training`、`match`、`admin`。`training` 可反复开始；`match` 每名玩家只能开始一次；`admin` 下所有人都是 ORT 管理员（不会自动授予原版 OP）。 |
| `courses` | object[] | 路线列表，见 3.2。 |


### 3.2 路线字段

| 字段 | 类型 | 说明 |
|---|---|---|
| `name` | string | 路线名，不能重复。 |
| `cplist` | int[] | 检查点编号列表。 |
| `coursetype` | string | `classic`、`score`、`fullpoint`、`team`。 |
| `validtime` | int | 有效时间，单位分钟。 |
| `start` | int[3] | 标定起点坐标；配合 `needteleport` 使用。 |
| `needteleport` | boolean | 是否传送到标定起点。`false` 时 `start` 不参与传送。 |
| `welcome` | string | 开始该路线时显示的欢迎文字，可省略。 |

`score` 积分赛额外字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `defaultscoresetting` | boolean | `true` 时分数取检查点编号的十位数；`false` 时使用 `scorelist`。 |
| `scorelist` | int[] | 自定义分数表，长度必须与 `cplist` 一致。 |
| `overtimepermit` | int | 允许超时的分钟数。 |
| `overtimepenalty` | int | 每超时一分钟扣除的分数。 |

`team` 团队赛额外字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `partitionedcp` | int[] | 团队共同检查点列表。 |

`fullpoint` 要求完成全部检查点，`team` 需要团队路线和团队检查点。

### 3.3 名册 roster.json

使用 `useplayerroster: true` 时，在比赛目录放置 `roster.json`：

```json
{
  "groups": [
    {
      "name": "PlayerA",
      "courses": ["course1"]
    },
    {
      "name": "TeamA",
      "courses": ["team-course-1", "team-course-2"]
    }
  ]
}
```

- 单人赛：`name` 是玩家 ID，`courses` 只有第一个元素有用。
- 接力/团队赛：`name` 是组名，玩家 ID 按 `组名-1`、`组名-2` 等规则生成，不能自定义。
- `courses` 的元素个数决定组内人数；不同组允许人数不同。
- 不在名册中的玩家不能开始比赛，管理员例外。

### 3.4 摔落伤害开关 customfalldamage

`customfalldamage` 控制 ORT 原有的比赛摔落规则：

- `true`（默认，保持旧行为）：
  - 原始摔落伤害 **大于最大生命值的一半** 时，直接造成 100 点 `OUT_OF_WORLD` 伤害，玩家死亡提示为“掉出了这个世界”。
  - 否则，取消原版摔落伤害，只造成原始伤害的一半。
- `false`：
  - 不拦截摔落事件，完全使用原版摔落伤害和原版死亡提示。

该字段只在生成比赛时读取。修改后必须重新生成比赛或新建比赛；只执行 `/ORT loadMatch` 不会更新已生成的比赛对象。重新生成会清空旧比赛数据。

## 4. 游戏内配置

### 4.1 config/orienteering.cfg

| 字段 | 类型 | 默认 | 说明 |
|---|---|---|---|
| `forceFoodLevel` | boolean | `false` | `false` 时不强制锁定饥饿值，原版或其他模组的饥饿机制正常工作；`true` 时比赛进行中每 tick 将饥饿值固定为 15，非比赛时固定为 20。 |

### 4.2 轨迹数据

轨迹记录数据保存在客户端的 `config/orienteering` 目录，不属于比赛目录。轨迹输出的 `_track.png` 会写入 `.orienteering`。

### 4.3 指北针

比赛进行中且比赛开启 `drawcompass` 时显示。默认 `U` 循环位置：右上 → 左上 → 左下 → 右下；`I` 显示/隐藏。两项均可在“选项 → 控制 → 按键设置 → OrienteeringMod”中修改。位置和显隐状态保存在客户端 `config/orienteering-client.cfg`。

## 5. 命令

| 命令 | 权限 | 说明 |
|---|---|---|
| `/ORT generate [比赛名]` | SP/MP | 生成并加载比赛。单机可省略比赛名，使用世界名；控制台执行时必须指定比赛名。 |
| `/ORT loadMatch [name]` | MP | 在专用服务器加载比赛；游戏内和控制台均可。 |
| `/ORT openMatch` | Admin | 宣布比赛开始；游戏内和控制台均可。 |
| `/ORT closeMatch` | Admin | 宣布比赛结束；游戏内和控制台均可。 |
| `/ORT home` | 非比赛中 | 立即传送回赛事中心。 |
| `/ORT clearPlayers` | Admin | 清除所有选手的比赛数据；游戏内和控制台均可。 |
| `/ORT getCourse` | 无 | 获取当前玩家的路线名。 |
| `/ORT changeCourse [name]` | 非接力、非进行中；Match 模式/有 roster 时仅管理员 | 切换自己的路线；成功后清除并重新发放该路线前缀的地图图片。 |
| `/ORT setCourse <player> <course>` | 游戏内管理员或服务器主控制台（不含命令方块）；目标在线 | 切换指定选手的路线并清除/重发地图；比赛中也可改，但不会重置或传送当前比赛状态，已有进度可能与新路线不匹配；仅单人群组。 |
| `/ORT changeMode` | Admin | 在 `training`、`match`、`admin` 之间切换比赛模式；游戏内和控制台均可。 |
| `/ORT stat` | 无 | 进行成统；游戏内和服务端控制台均可执行。 |
| `/ORT sketch` | SP | 扫描地图；仅单人且当前没有加载比赛时可用。 |
| `/ORT stuck` | 已加载比赛 | 卡住时随机传送到附近地表；比赛进行中按 `deathpenalty` 记录一次死亡扣时，不在赛中仅提示 `Teleporting...`。 |
| `/ORT back` | 比赛中 | 传送到最近一次打卡的检查点；尚未打卡时传送到实际开赛位置（没有记录时回退赛事中心）。按 `backpenalty` 扣时并计入最终成绩。 |

可在服务端控制台执行的指令：`generate`、`loadMatch`、`openMatch`、`closeMatch`、`clearPlayers`、`changeMode`、`stat`。其余指令需要以玩家身份执行。

### Tab 补全

`/ORT` 支持 Tab 补全：

- 第一层：游戏内补全全部子命令；服务端控制台只补全可在控制台执行的子命令。
- 补全不区分大小写，候选按字母排序。
- `/ORT loadMatch <Tab>` 补全 `.orienteering` 中已有 `match` 文件的比赛名。
- `/ORT generate <Tab>` 补全已有 `matchseed.json` 的比赛名。
- `/ORT changeCourse <Tab>` 补全当前比赛的路线名。
- `/ORT setCourse <Tab>` 先补全在线玩家名，再补全当前比赛的路线名。

## 6. 方块和物品

- **检查点方块**：黄色，带 TileEntity，用于记录点号。放置时会弹出输入界面，编号范围 0–999；输入完成后不能修改。
- **起点**：紫色。玩家点击后检查比赛规则，满足条件则传送到标定起点并开始比赛。
- **终点**：蓝色。点击后结束比赛并传送回赛事中心。
- **主站方块**：用于计算成绩和输出轨迹。
- **地图领取台**：纹理沿用原版制图台；右键清除玩家物品栏中的所有地图图片，再按当前路线名作为前缀重新发放。
- **时钟**：右键将时间设置为 23250（日出），任何时候可用。
- **铲子**：右键破坏方块且不掉落，冷却 2 分钟。
- **地图图片**：玩家右键地图领取台时，以及 `/ORT changeCourse`（自己）或 `/ORT setCourse <玩家> <路线>`（目标玩家）成功后，先清除对应玩家物品栏中的所有地图图片（含副手和装备栏），再以该玩家当前路线名作为文件名前缀（不区分大小写），从 `.orienteering/<比赛名>/maps/` 中筛选 PNG/JPG/JPEG（文件非空且不超过 64 MiB）。每张匹配图片生成一个同名物品（去掉扩展名），物品 NBT 记录比赛名和原始文件名，物品在主手/副手显示为地图样式；背包满时掉落在脚下。开赛不再发放或刷新地图；比赛加载或玩家中途加入也不会统一补发。

### 6.1 地图图片操作

手持任意一张地图图片物品即可操作。以下为默认键位，可在“选项 → 控制 → 按键设置 → OrienteeringMod”中修改；H 模式顶部提示会显示当前绑定键位，双手各持不同地图时还会同时显示两张图的缩放比例。

- `J`：逆时针旋转 90°。
- `K`：顺时针旋转 90°。
- 主手持图且副手为空时，自动切换为原版地图的双手持图姿势。
- 单手持有地图时（主手有图且副手有物品，或地图在副手），地图以平面纸张样式显示在对应侧，另一只手的物品正常显示。
- 按 `H` 解锁鼠标，滚轮以光标在地图上的实际位置为中心缩放该地图（缩到整张图完整显示后不能再缩小）；缩放结果在退出 `H` 模式后保留，再次按 `H` 恢复鼠标。双手和单手姿势都可用。
- 按 `H` 解锁鼠标后，已放大的地图可按住左键拖动平移，平移结果同样保留；双手都有地图时只拖动光标下的那张，两张重叠时拖动地图中心离光标更近的一张。拖动开始后光标移出地图仍会继续平移；光标不在任何地图上时按下左键不会开始拖动。
- 主手和副手都有地图时，两张地图会同时响应 `J`/`K` 旋转；按 `H` 解锁鼠标后，滚轮在某一侧地图上时只缩放该张，光标不在任一张地图上时两张同时以各自中心缩放；两张为同一图片时共享缩放状态。
- 图片首次被手持或打开查看时由服务器分块传输到客户端，之后使用客户端缓存；大图首次加载会有一小段等待时间。

客户端不保存玩家比赛状态；比赛判定在服务端进行。准星指向起点、终点或检查点时，会在准星下方显示中文名称，检查点同时显示编号。

## 7. 主站和成统

- 玩家每次开始比赛会清除之前的成绩；比赛结束后点击主站才会计算成绩。
- 成绩一旦计算不会重复计算，直到玩家再次开始比赛。
- 接力/团队赛会把同组队友的成绩合并；未完成的队友会被跳过。
- 服务器比赛完成后执行 `/ORT stat`，在比赛目录生成：
  - `0_stat.txt`：总成绩，可用 Excel 打开；列结构见 7.1。
  - `1_statdetail.txt`：每个检查点的明细；文件末尾的 `# Back records` 段记录 `/ORT back` 的比赛时间点、目标点号或 START/SPAWN、目标坐标和传送前坐标；列结构见 7.2。
- 每名玩家的成绩条文件也会列出 back 使用记录。
- 成统明细写入 `1_statdetail.txt`；读取玩家数据出错时，错误写入服务器日志。
- 线下比赛需要手动收集 `.orienteering/<比赛名>/players/<玩家 ID>` 数据。

### 7.1 0_stat.txt

每个成绩分组一行，制表符分隔，**没有表头**；时间格式为 `HH:MM:SS.d`（精度 0.1 秒）。行序是内存中成绩表的顺序，不保证按玩家名或成绩排序。

**个人赛 `Classic` / `Full point`（9 列）**

| 列 | 内容 |
|---|---|
| 1 | 比赛名 |
| 2 | 类型：`Classic` 或 `Full point` |
| 3 | 路线名 |
| 4 | 玩家名 |
| 5 | 原始用时（未计罚时） |
| 6 | 死亡次数 |
| 7 | 回溯次数（`/ORT back` 记录数） |
| 8 | 罚时后用时 |
| 9 | 是否合格：`T` / `F` |

**个人赛 `Score`（10 列）**

前 9 列同上，第 10 列为最终得分；得分可能因超时扣分为负数。类型列显示 `Score`。

**接力赛（3 列）**

| 列 | 内容 |
|---|---|
| 1 | 组名 |
| 2 | 合格人数 |
| 3 | 组总用时：各成员罚时后用时之和 |

**团队赛（3 列）**

| 列 | 内容 |
|---|---|
| 1 | 组名 |
| 2 | 团队是否合格：`T` / `F` |
| 3 | 组总用时：成员罚时后用时中的最大值 |

说明：

- 罚时后用时 = 原始用时 + 死亡次数 × 每次死亡罚时 + `back` 罚时，具体罚时由比赛和路线配置决定。
- 合格判定：
  - `Classic`：罚时后用时不超过有效时间，按 `cplist` 顺序完成全部检查点并完成比赛。
  - `Full point`：罚时后用时不超过有效时间，集齐全部检查点（顺序不限）并完成比赛。
  - `Score`：罚时后用时不超过有效时间加 `overtimepermit`，完成比赛；分数按不重复检查点累计，超时按分钟扣分。
  - 接力：各棒独立判定，`0_stat` 汇总合格人数和各棒罚时后用时之和。
  - 团队：所有成员个人合格，且团队分点 `partitionedcp` 全部收集；组总用时取成员中的最大值。
- 执行 `/ORT stat` 时，已有分组成绩不会重算，缺少的分组会补算；没有可导出成绩的分组不会写入。
- 示例（实际分隔符为制表符）：

```text
race_world	Classic	open	player1	00:21:33.1	0	0	00:21:33.1	T
race_world	Classic	elite	player2	01:28:17.2	1	2	01:43:17.2	F
```

### 7.2 1_statdetail.txt

先写逐点明细，再写 `# Back records` 段；制表符分隔。

**逐点明细**

每个可导出的个人成绩一行，格式为：

```text
比赛名	路线名	玩家名	点号	分段用时	点号	分段用时	...
```

- 点号按实际打卡顺序记录；完成点记为 `EN`（内部值 `-1`）。
- 分段用时是距上一个检查点或起点的用时，不是累计用时。
- 每对“点号 + 分段用时”后都有一个制表符，因此行尾通常会多出一个空字段。
- 接力/团队没有逐点明细，在对应位置只写空行。
- 逐点明细的行序与 `0_stat` 的分组顺序一致，不保证排序。

**Back records**

全部逐点明细之后是空行、`# Back records` 和一行表头：

```text
# match	player	course	index	match_time	target	target_x	target_y	target_z	from_x	from_y	from_z
```

每行记录一次 `/ORT back`：

| 列 | 内容 |
|---|---|
| 1 | 比赛名 |
| 2 | 玩家名 |
| 3 | 路线名 |
| 4 | 该玩家的回溯序号，从 1 开始 |
| 5 | 回溯时的比赛用时（距开赛，不是现实时间） |
| 6 | 目标：`CP <编号>`、`START` 或 `SPAWN` |
| 7–9 | 目标坐标 `x/y/z` |
| 10–12 | 传送前坐标 `x/y/z` |

- 坐标保留 1 位小数。
- 回溯记录按玩家名排序，同一玩家内按发生顺序排列。
- 示例（制表符分隔）：

```text
race_world	player2	elite	1	00:12:11.0	CP 38	636.1	80.0	-1150.4	479.8	80.0	-900.4
```

## 8. 轨迹功能

- 轨迹模块只在客户端存在，数据保存在 `config/orienteering`。
- 每 0.5 秒记录一个轨迹点；玩家开始比赛时清空旧轨迹。
- 点击主站时输出到 `.orienteering/<比赛名>_track.png`。
- 轨迹导出会自动创建目录、使用实际轨迹点计算边界，并对空轨迹、并发更新和非法比赛名做保护；异常会写入 `OrienteeringMod-Track` 日志。

## 9. 制图功能

制图会扫描存档并把不同方块用不同颜色表示，输出：

- `ground.png`：人工认定的地面方块。
- `foliage.png`：按配置显示树冠与地表，并叠加以世界 Y 坐标为基准、等高距 5 格的棕色等高线。
- `ground-height.png`：每 1 格高度单独设色，附本次扫描的高程图例。
- `ground-height.csv`：首行为世界 X 坐标，首列为 Z 坐标，每个单元格记录对应地面的 Y 值。

高程从上往下取有效地面，跳过树冠、树干等覆盖物。优先使用 Y > 50 的地面，上方没有有效地面时才向下查找。

制图只在单人可用；检测到比赛文件时禁用，防止比赛中使用。扫描区域必须提前生成，否则区块生成可能导致游戏崩溃。制图可能耗时一分钟以上。

`cartography.json` 示例：

```json
{
  "regionNW": [-500, -500],
  "regionSE": [500, 500],
  "maxairgap": 5,
  "displaygap": ["minecraft:leaves"],
  "decrease": 3,
  "emph": ["minecraft:log"],
  "ground": [
    { "material": "minecraft:grass", "R": 90, "G": 150, "B": 60 },
    { "material": "minecraft:dirt", "R": 130, "G": 90, "B": 60 }
  ],
  "penetrate": ["minecraft:air"],
  "foliage": [
    { "material": "minecraft:leaves", "R": 40, "G": 120, "B": 30 }
  ]
}
```

字段说明：

| 字段 | 说明 |
|---|---|
| `regionNW` / `regionSE` | 扫描区域左上角和右下角的 `[x, z]` 坐标。 |
| `maxairgap` | 连续空气达到该值后，空气底部视为地面。 |
| `displaygap` | 用 gap 深度调整颜色的方块列表。 |
| `decrease` | 每格 gap 对 RGB 的偏移量，省略时取 0。 |
| `emph` | 探测到这些方块时立即停止。 |
| `ground` | 地面方块及其 RGB 颜色。 |
| `penetrate` | 视为空气、忽略的方块。 |
| `foliage` | 从高向低探测时优先显示的方块及颜色。未单独配置 `minecraft:leaves2` 时沿用 `minecraft:leaves` 的颜色。 |

填写后在存档内执行 `/ORT sketch`，看到 `Export complete` 提示后，可在对应比赛目录查看四个输出文件。
