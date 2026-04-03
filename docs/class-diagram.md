# TaskMaster — Class & Entity Relationship Diagram

## Entity Relationship Diagram (ERD)

```
┌──────────────────────┐         ┌──────────────────────────┐
│         User         │         │           Team            │
├──────────────────────┤         ├──────────────────────────┤
│ id            : Long │◄───────►│ id          : Long        │
│ username      : String│  M:M   │ name        : String      │
│ email         : String│        │ description : String      │
│ password      : String│        │ owner       : User (FK)   │
│ fullName      : String│        │ members     : Set<User>   │
│ bio           : String│        │ inviteCode  : String      │
│ role          : Enum  │        │ createdAt   : LocalDateTime│
│ enabled       : Boolean│       └────────────┬─────────────┘
│ createdAt             │                     │ 1:N
└───────────┬───────────┘                     │
            │ 1:N                   ┌──────────▼──────────────┐
            │                       │          Task            │
┌───────────▼───────────┐           ├─────────────────────────┤
│      Notification     │           │ id          : Long       │
├───────────────────────┤           │ title       : String     │
│ id        : Long      │           │ description : Text       │
│ message   : String    │           │ status      : Enum       │
│ type      : Enum      │           │ priority    : Enum       │
│ recipient : User (FK) │           │ dueDate     : LocalDate  │
│ taskId    : Long      │           │ creator     : User (FK)  │
│ isRead    : Boolean   │           │ assignee    : User (FK)  │
│ createdAt             │           │ team        : Team (FK)  │
└───────────────────────┘           │ createdAt               │
                                    │ updatedAt               │
                                    │ completedAt             │
                                    └────────────┬────────────┘
                                                 │ 1:N
                              ┌──────────────────┴───────────────────┐
                              │ 1:N                               1:N │
                   ┌──────────▼────────┐                ┌────────────▼────────┐
                   │      Comment      │                │      Attachment      │
                   ├───────────────────┤                ├─────────────────────┤
                   │ id       : Long   │                │ id          : Long   │
                   │ content  : Text   │                │ fileName    : String │
                   │ task     : Task   │                │ originalName: String │
                   │ author   : User   │                │ fileType    : String │
                   │ createdAt         │                │ fileSize    : Long   │
                   │ updatedAt         │                │ filePath    : String │
                   └───────────────────┘                │ task        : Task   │
                                                        │ uploader    : User   │
                                                        │ createdAt           │
                                                        └─────────────────────┘
```

## Task Status Lifecycle

```
  ┌──────┐     ┌─────────────┐     ┌────────┐     ┌───────────┐
  │ OPEN │────►│ IN_PROGRESS │────►│ REVIEW │────►│ COMPLETED │
  └──┬───┘     └─────────────┘     └────────┘     └───────────┘
     │
     ▼
 ┌──────────┐
 │ CANCELLED│
 └──────────┘
```

## Priority Levels
`LOW` → `MEDIUM` → `HIGH` → `URGENT`

## Notification Type Enum
| Type | Trigger |
|---|---|
| TASK_ASSIGNED | A task is assigned/reassigned |
| TASK_UPDATED | Any task field is changed |
| TASK_COMPLETED | Task status set to COMPLETED |
| COMMENT_ADDED | New comment on a task |
| ATTACHMENT_ADDED | File uploaded to a task |
| TEAM_INVITE | User added to a team by owner |
| TEAM_JOINED | User joined via invite code |

## Service Layer Architecture

```
AuthController  ──► AuthService  ──► UserRepository / JwtTokenProvider
UserController  ──► UserService  ──► UserRepository
TaskController  ──► TaskService  ──► TaskRepository / NotificationService
TeamController  ──► TeamService  ──► TeamRepository / NotificationService
CommentController──►CommentService──►CommentRepository / NotificationService
AttachmentController►AttachmentService►AttachmentRepository
NotificationController►NotificationService►NotificationRepository / SimpMessagingTemplate
```
