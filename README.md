# 个人求职助手

个人求职助手是一款本地运行的 Windows 桌面端求职进度管理工具，用于管理岗位投递、招聘流程、简历文件、面试记录、笔记资料和提醒事项。

项目最初面向秋招投递管理，当前已经扩展为更通用的个人求职管理客户端，适用于秋招、春招、实习、提前批、社招、国企、银行、研究所等多种投递场景。

## 项目特点

- 本地桌面客户端，数据默认保存在用户电脑中。
- 支持投递记录全流程管理：岗位信息、投递状态、招聘流程、面试记录、JD、备注。
- 支持简历文件管理和岗位绑定简历。
- 支持独立笔记管理，类似轻量文件管理器。
- 支持 Markdown/TXT/PDF/Word 笔记上传、预览、管理。
- 支持提醒事项和全局未完成预警。
- 支持生成 Windows 安装包，并提供 lite/full 两种版本。

## 技术架构

桌面端采用：

- Electron：Windows 桌面窗口和本地进程管理
- Vue 3 + TypeScript + Element Plus：前端界面
- Spring Boot 3：本地后端服务
- MyBatis Plus：数据访问层
- H2：桌面端本地文件数据库
- 本地文件系统：保存简历、面试笔记、普通笔记和上传文件

运行方式：

```text
Electron 客户端
   ↓ 启动本地 Java 进程
Spring Boot 后端 127.0.0.1:18080
   ↓
H2 本地数据库 + 本地文件目录
```

桌面端启动后，Electron 会自动启动 Spring Boot jar，并打开客户端窗口。用户通常不需要手动启动后端或前端。

## 目录结构

```text
JobTrackerDesktop/
├─ backend/                 Spring Boot 后端
│  ├─ src/main/java/        后端业务代码
│  ├─ src/main/resources/   后端配置、H2 初始化脚本
│  └─ pom.xml               Maven 项目配置
├─ frontend/                Vue 3 前端
│  ├─ src/                  前端源码
│  ├─ index.html            Vite 入口页面
│  ├─ package.json          前端依赖和脚本
│  └─ vite.config.ts        Vite 配置
├─ desktop/                 Electron 桌面端封装和打包脚本
│  ├─ main.js               Electron 主进程
│  ├─ preload.js            桌面端桥接能力
│  ├─ build-desktop.ps1     通用打包脚本
│  ├─ build-desktop-lite.ps1 不内置 JRE 打包入口
│  ├─ build-desktop-full.ps1 内置 JRE 打包入口
│  ├─ electron-builder.lite.yml
│  ├─ electron-builder.full.yml
│  └─ package.json          桌面端依赖和打包脚本
├─ database/                MySQL 建表和迁移脚本
├─ .gitignore               Git 忽略规则
├─ END_USER_MANUAL.md       面向最终用户的使用说明
└─ README.md                项目说明
```

## 核心功能

### Dashboard

- 总投递数、面试数、Offer 数统计
- 投递状态分布
- 每周投递趋势
- 公司投递统计
- 最近投递记录
- 今日提醒
- 未完成提醒全局预警

### 投递管理

- 新增、编辑、删除投递记录
- 支持岗位类别多选和自定义
- 支持简历类别多选和自定义
- 支持投递批次、投递状态自定义
- 支持绑定简历版本
- 支持 Markdown 岗位 JD
- 支持按公司名、状态、岗位类别、投递批次、简历类别、投递时间范围查询
- 支持按投递时间和公司名排序
- 支持按公司合并展示

### 投递详情

- 查看岗位完整信息
- 预览绑定的 PDF 简历
- Markdown 渲染岗位 JD
- 投递进度时间轴
- 自定义招聘流程
- 当前进度、成功/失败结果、操作时间写回详情页
- 面试记录管理
- Markdown 面试笔记管理

### 简历管理

- 上传简历文件
- 编辑版本名称和备注
- PDF 简历预览
- 下载简历
- 删除简历
- 在投递记录中绑定简历

### 笔记管理

笔记管理是独立于岗位面试笔记的普通笔记区。

支持：

- 新建文件夹
- 新建笔记
- 上传笔记
- 编辑文件信息
- 拖拽移动文件夹和笔记
- 移出到根目录
- 下载文件
- 删除文件或文件夹

支持文件类型：

| 类型 | 预览 | 编辑内容 |
| --- | --- | --- |
| `.md` / `.markdown` | 支持 | 支持 |
| `.txt` | 支持 | 支持 |
| `.pdf` | 支持 | 不支持 |
| `.doc` / `.docx` | 支持提取文本预览 | 不支持 |

PDF 和 Word 文件不能直接编辑正文，但可以编辑名称、所在位置等文件信息。

### 提醒管理

- 新增提醒
- 编辑提醒
- 标记完成
- 删除提醒
- 完成/未完成状态高亮
- 存在未完成提醒时显示全局预警

### 系统设置

支持可视化设置本地保存位置：

- 简历保存目录
- 面试笔记保存目录
- 普通笔记保存目录

## 安装包版本

当前支持两种 Windows 安装包。

| 版本 | 文件名 | 说明 |
| --- | --- | --- |
| lite | `个人求职助手 Setup 0.1.0-lite.exe` | 不内置 JRE，适合已安装 Java 17 的电脑 |
| full | `个人求职助手 Setup 0.1.0-full.exe` | 内置精简 JRE 17，适合普通用户，推荐分发 |

两个安装包保持同一个应用身份：

```text
appId: com.jobtracker.desktop
productName: 个人求职助手
```

因此，用户安装 lite 后再安装 full，或者安装 full 后再安装 lite，都会被视为同一个应用的覆盖安装。用户本地数据通常会继续沿用。

注意：如果从 full 切换到 lite，目标电脑必须已有 Java 17，否则客户端可能无法启动。

## 用户数据位置

桌面端默认使用 Electron 的用户数据目录，例如：

```text
C:\Users\用户名\AppData\Roaming\个人求职助手\
```

通常包含：

```text
jobtracker.mv.db          H2 本地数据库
resumes\                  简历文件
interview_notes\          岗位详情里的面试笔记
notes\                    普通笔记文件
storage.properties        保存目录配置
```

如果用户在系统设置中修改了保存目录，新文件会保存到新的自定义目录。已有文件不会自动迁移。

## 开发环境

开发和打包需要：

- Windows 10 / Windows 11
- Java 17 JDK
- Maven 3.9+
- Node.js / npm
- Electron Builder

请确保 `java`、`mvn`、`node`、`npm` 可以在终端中正常使用。也可以在执行打包脚本时通过参数传入本机 Maven 和 JDK 工具路径。


## 本地配置

桌面端打包和正常运行时使用 `desktop` profile，默认是 H2 本地文件数据库，一般不需要配置 MySQL 密码。

如果把 `backend` 当作 Web 后端连接本机 MySQL 运行，可以复制 Spring Boot 配置模板，并填写本机 MySQL 密码：

```powershell
Copy-Item backend\src\main\resources\application.example.yml backend\src\main\resources\application.yml
```

启动 MySQL 版后端时使用 `local` profile：

```powershell
cd <PROJECT_ROOT>\backend
mvn spring-boot:run
```
## 本地开发运行

### 后端

```powershell
cd <PROJECT_ROOT>\backend
mvn spring-boot:run
```

### 前端

```powershell
cd <PROJECT_ROOT>\frontend
npm.cmd install
npm.cmd run dev
```

前端默认地址：

```text
http://localhost:5173
```

后端默认地址：

```text
http://localhost:8080
```

桌面端打包后使用 `desktop` profile，默认端口为 `18080`。

## 打包 Windows 客户端

进入桌面端目录：

```powershell
cd <PROJECT_ROOT>\JobTrackerDesktop\desktop
```

打包 lite 版：

```powershell
.\build-desktop-lite.ps1
```

打包 full 版：

```powershell
.\build-desktop-full.ps1
```

也可以使用通用脚本：

```powershell
.\build-desktop.ps1 -Flavor lite
.\build-desktop.ps1 -Flavor full
```

输出目录：

```text
<PROJECT_ROOT>\desktop\release
```

预期产物：

```text
个人求职助手 Setup 0.1.0-lite.exe
个人求职助手 Setup 0.1.0-full.exe
```

## 文档

- `END_USER_MANUAL.md`：面向最终用户的完整使用说明
- `WINDOWS_CLIENT_USER_GUIDE.md`：Windows 客户端安装和环境说明
- `README_DESKTOP.md`：桌面端架构和打包说明

## 备份建议

建议用户定期备份：

```text
jobtracker.mv.db
resumes\
interview_notes\
notes\
storage.properties
```

如果用户设置了自定义保存目录，也需要一起备份对应目录。

## 注意事项

- 桌面版默认本地运行，不需要 MySQL。
- lite 版需要目标电脑已有 Java 17。
- full 版已内置精简 JRE 17，适合直接交付普通用户。
- 修改保存位置只影响新文件，不自动迁移旧文件。
- 删除文件夹会同时删除其子项目，请谨慎操作。
- 当前安装包未配置自定义应用图标，Electron Builder 会使用默认图标。



