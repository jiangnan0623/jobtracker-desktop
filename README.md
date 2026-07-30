<p align="center">
  <img src="./assets/readme/hero.png" width="100%" alt="JobTracker Desktop，本地运行的个人求职进度管理工具">
</p>

<p align="center">
  <strong>Windows 10 / 11</strong> · Electron · Vue 3 · Spring Boot 3 · H2
</p>

<p align="center">
  <a href="#核心能力">核心能力</a> ·
  <a href="#安装与运行">安装与运行</a> ·
  <a href="#数据与备份">数据与备份</a> ·
  <a href="./END_USER_MANUAL.md">用户手册</a>
</p>

JobTracker Desktop（个人求职助手）是一款本地优先的 Windows 求职进度管理工具。它把岗位投递、招聘流程、简历版本、面试记录、笔记资料和日程提醒放进同一个桌面客户端，帮助你随时回答：

- 投了哪些公司和岗位？
- 每条投递进行到哪一步？
- 当时使用的是哪份简历？
- 下一场笔试、面试或截止事项是什么时候？

适用于秋招、春招、实习、提前批、社招、国企、银行、研究所等需要长期跟踪多条投递的场景。应用在本机启动服务，数据默认不会主动上传到远程服务器。

## 一眼看清所有投递

<p align="center">
  <img src="./assets/readme/screenshots/applications.png" width="100%" alt="JobTracker Desktop 投递管理界面，展示岗位筛选、状态标签和投递列表">
</p>

> 截图使用演示数据，不包含真实个人求职信息。

筛选、排序和状态标签让投递台账保持可扫描；同一公司的多个岗位也可以合并查看。进入岗位详情后，可以继续维护 JD、招聘流程、面试记录、复盘笔记和关联日程。

<p align="center">
  <img src="./assets/readme/workflow.svg" width="100%" alt="从发现岗位、绑定简历、跟进流程、安排日程到复盘归档的完整工作流">
</p>

## 核心能力

- **投递管理**：记录公司、岗位、批次、来源、地点、薪资、JD 和备注，支持组合筛选、自定义状态、排序与按公司合并。
- **招聘流程**：为每个岗位维护独立流程，用时间轴记录节点、结果和操作时间，并处理 Offer 与淘汰等最终状态。
- **简历版本**：上传、预览、下载和维护不同版本的简历；投递时可绑定已有简历，也可临时上传并自动绑定。
- **面试与复盘**：记录轮次、时间、面试官、问题、难度和结果，并为每个岗位保存 Markdown 面试笔记。
- **笔记资料**：以轻量文件管理器的方式组织文件夹、Markdown、TXT、PDF 和 Word 资料，支持预览、移动与下载。
- **日程联动**：使用月、周、日和列表视图管理笔试、面试、复盘与截止日期，并把事项关联回具体岗位。
- **全局概览**：查看投递总量、状态分布、每周趋势、公司统计、最近投递和未完成日程预警。

## 把下一步放进日历

<p align="center">
  <img src="./assets/readme/screenshots/schedule.png" width="100%" alt="JobTracker Desktop 日程管理月视图，展示统计、优先级和岗位相关日程">
</p>

日程可以设置起止时间、优先级、重要程度和关联投递。未完成事项会在应用顶部持续提醒；完成或取消后，它们会自动离开待处理列表。

## 安装与运行

### Windows 安装包

项目支持生成两种安装包：

| 版本 | 适合谁 | 运行要求 |
| --- | --- | --- |
| `full` | 希望开箱即用的普通用户 | 自带精简 Java 运行时 |
| `lite` | 已安装 Java 17 的用户 | 电脑需要可用的 Java 17 |

构建完成后，安装包会出现在 `desktop/release/`。双击安装并按向导完成后，即可从桌面快捷方式或开始菜单打开“个人求职助手”。

### 本地开发

环境要求：

- Windows 10 / 11
- Java 17 JDK
- Maven 3.9+
- Node.js 与 npm

启动后端：

```powershell
cd backend
mvn spring-boot:run
```

在另一个终端启动前端：

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

开发页面默认位于 `http://localhost:5173`，后端默认位于 `http://localhost:8080`。

### 构建桌面安装包

```powershell
.\desktop\build-desktop-full.ps1
```

或构建不携带 Java 运行时的轻量版本：

```powershell
.\desktop\build-desktop-lite.ps1
```

构建脚本会依次完成前端打包、后端打包、Electron 资源整理和 Windows 安装包生成。

## 工作方式

```text
Electron 客户端
    │
    ├── 启动本地 Spring Boot 服务（127.0.0.1:18080）
    │
    ├── 读写 H2 本地数据库
    │
    └── 管理简历、面试笔记与普通笔记文件
```

桌面客户端启动后会自动启动本地后端服务并打开应用窗口。普通使用者不需要单独配置远程服务器或 MySQL。

## 数据与备份

默认数据目录通常位于：

```text
C:\Users\你的用户名\AppData\Roaming\个人求职助手\
```

建议定期备份：

```text
jobtracker.mv.db
resumes\
interview_notes\
notes\
storage.properties
```

如果在系统设置中修改过保存目录，也需要一并备份对应目录。完整的迁移步骤见 [用户使用说明](./END_USER_MANUAL.md#十二换电脑怎么办)。

## 使用边界

- 当前桌面打包面向 Windows。
- PDF 和 Word 文件支持预览与管理，不支持直接编辑正文。
- 修改文件保存位置只影响之后创建或上传的文件，不会自动迁移已有文件。
- 删除文件夹会同时删除其子项目，请在操作前确认并做好备份。
- 日程只有明确绑定到投递记录后，才会出现在对应岗位详情中。

## 项目结构

```text
JobTrackerDesktop/
├── backend/        Spring Boot 后端与 H2 数据访问
├── frontend/       Vue 3 桌面界面
├── desktop/        Electron 入口、运行时与安装包脚本
├── database/       数据库脚本与迁移
└── END_USER_MANUAL.md
```

## 技术栈

- **桌面端**：Electron
- **前端**：Vue 3、TypeScript、Vite、Element Plus、ECharts
- **后端**：Java 17、Spring Boot 3、MyBatis Plus
- **本地存储**：H2 与本地文件系统

## 文档

- [最终用户使用说明](./END_USER_MANUAL.md)
- [MIT License](./LICENSE)

## License

本项目基于 [MIT License](./LICENSE) 开源。
