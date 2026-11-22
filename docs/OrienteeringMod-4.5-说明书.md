# OrienteeringMod 4.5 使用说明

> 本文档对应 OrienteeringMod 1.12.2 4.5。Word 版仅保留作旧格式参考；配置字段、命令和代码示例以本 Markdown 为准。

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
   ├─ maps/                       选手开赛时发放的图片（PNG/JPG/JPEG）
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
| `/ORT changeCourse [name]` | 非接力、非进行中 | 切换路线。 |
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

## 6. 方块和物品

- **检查点方块**：黄色，带 TileEntity，用于记录点号。放置时会弹出输入界面，编号范围 0–999；输入完成后不能修改。
- **起点**：紫色。玩家点击后检查比赛规则，满足条件则传送到标定起点并开始比赛。
- **终点**：蓝色。点击后结束比赛并传送回赛事中心。
- **主站方块**：用于计算成绩和输出轨迹。
- **时钟**：右键将时间设置为 23250（日出），任何时候可用。
- **铲子**：右键破坏方块且不掉落，冷却 2 分钟。
- **地图图片**：开赛时从 `.orienteering/<比赛名>/maps/` 读取 PNG/JPG/JPEG，每张图片生成一个同名物品（去掉扩展名）。物品在主手/副手显示为地图样式；已经拥有的同名图片不会重复发放，背包满时掉落在脚下。

### 6.1 地图图片操作

手持任意一张地图图片物品即可操作：

- `J`：逆时针旋转 90°。
- `K`：顺时针旋转 90°。
- `H`：解锁鼠标，在地图叠层上查看；鼠标位置为缩放中心，滚轮缩放。缩到整张图片完整显示时不能再缩小；再次按 `H` 恢复鼠标。
- 主手持图且副手为空时，自动切换为原版地图的双手持图姿势；副手有物品时保持单手持有。
- 主手和副手都有地图时，两张地图会同时响应旋转；`H` 查看主手地图，主手没有时查看副手地图。
- 图片首次被手持或打开查看时由服务器分块传输到客户端，之后使用客户端缓存；大图首次加载会有一小段等待时间。

客户端不保存玩家比赛状态；比赛判定在服务端进行。准星指向起点、终点或检查点时，会在准星下方显示中文名称，检查点同时显示编号。

## 7. 主站和成统

- 玩家每次开始比赛会清除之前的成绩；比赛结束后点击主站才会计算成绩。
- 成绩一旦计算不会重复计算，直到玩家再次开始比赛。
- 接力/团队赛会把同组队友的成绩合并；未完成的队友会被跳过。
- 服务器比赛完成后执行 `/ORT stat`，在比赛目录生成：
  - `0_stat.txt`：总成绩，可用 Excel 打开。
  - `1_statdetail.txt`：每个检查点的明细；文件末尾的 `# Back records` 段记录 `/ORT back` 的比赛时间点、目标点号或 START/SPAWN、目标坐标和传送前坐标。
- 每名玩家的成绩条文件也会列出 back 使用记录。
- 成统明细写入 `1_statdetail.txt`；读取玩家数据出错时，错误写入服务器日志。
- 线下比赛需要手动收集 `.orienteering/<比赛名>/players/<玩家 ID>` 数据。

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
