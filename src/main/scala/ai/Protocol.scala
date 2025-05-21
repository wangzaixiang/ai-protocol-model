package ai

import wjson.JsValue
import wjson.JsValue.JsObject

object a2a {

  trait A2AProtocol {
    def `message/send`(message: Message, configuration: MessageSendConfiguration | Null, metadata: JsObject|Null): Task | Message
    def `message/stream`(message: Message, configuration: MessageSendConfiguration | Null, metadata: JsObject|Null): Iterator[Message | Task | TaskStatusUpdateEvent | TaskArtifactUpdateEvent]
    def `tasks/get`(id: String, historyLength: Int|Null, metadata: JsObject|Null): Task
    def `tasks/cancel`(id: String, metadata: JsObject|Null): Task
    def `tasks/pushNotificationConfig/set`(params: TaskPushNotificationConfig): TaskPushNotificationConfig
    def `tasks/pushNotificationConfig/get`(id: String, metadata: JsObject|Null): TaskPushNotificationConfig
    def `tasks/resubscribe`(id: String, metadata: JsObject|Null): Iterator[Message | Task | TaskStatusUpdateEvent | TaskArtifactUpdateEvent]
  }

  // recommended location: https://{server_domain}/.well-known/agent.json
  case class AgentCard
  (
    name: String,
    description: String,
    url: String,
    provider: AgentProvider | Null,
    version: String,
    documentationUrl: String | Null,
    capabilities: AgentCapabilities,
    securitySchemes: Map[String, SecurityScheme] | Null,  // TODO
    security: Map[String,  List[String] ] | Null, // TODO
    defaultInputModes: List[String],
    skills: List[AgentSkill]
  )

  case class AgentProvider
  (
    organization: String,
    url: String
  )
  case class AgentCapabilities
  (
    streaming: Boolean | Null,
    pushNotifications: Boolean | Null,
    stateTransitionHistory: Boolean | Null
  )

  // TODO
  enum SecurityScheme:
    case APIKeySecurityScheme
    case HTTPAuthSecurityScheme
    case OAuth2SecurityScheme
    case OpenIdConnectSecurityScheme

  case class AgentSkill
  (
    id: String,
    description: String,
    tags: List[String],
    examples: List[String]|Null,
    inputModes: List[String]|Null,
    outputModes: List[String] | Null
  )

  /**
   * The fundamental unit of work managed by A2A, identified by a unique ID. Tasks are stateful and progress through a defined lifecycle.
   */
  case class Task
  (
    id: String,
    contextId: String,
    status: TaskStatus,
    artifacts: List[Artifact] | Null,
    history: List[Message] | Null,
    metadata: JsObject | Null
  )

  case class TaskStatus
  (
    state: TaskState,
    message: Message | Null,
    timestamp: String | Null,
  )

  enum TaskState:
    case submitted
    case working
    case `input-required`
    case completed
    case canceled
    case failed
    case rejected
    case `auth-required`
    case unknown

  case class Artifact
  (
    artifactId: String,
    name: String | Null,
    description: String | Null,
    parts: List[Part],
    metadata: JsObject | Null
  )

  enum Part:
    case TextPart(text: String, metadata: JsObject | Null)
    case FilePart(file: FileWithBytes | FileWithUri, metadata: JsObject | Null)
    case DataPart(data: JsObject, metadata: JsObject | Null)

  case class FileWithBytes(name: String|Null, mimeType: String|Null, bytes: String)
  case class FileWithUri(name: String|Null, mimeType: String|Null, uri: String)

  /**
   * A communication turn between a client and a remote agent, having a role ("user" or "agent") and containing one or more Parts
   */
  case class Message
  (
    role: "user" | "agent",
    parts: List[Part],
    metadata: JsObject | Null,
    messageId: String,
    taskId: String | Null,
    kind: "message"
  )

  case class MessageSendConfiguration
  (
    acceptedOutputModes: List[String],
    historyLength: Int | Null,
    pushNotificationConfig: PushNotificationConfig | Null,
    blocking: Boolean | Null
  )

  case class PushNotificationConfig
  (
    url: String,
    token: String | Null,
    authentication: AuthenticationInfo | Null
  )

  case class AuthenticationInfo
  (
    schemes: List[String],
    credentials: String | Null
  )

  case class TaskStatusUpdateEvent
  (
    taskId: String,
    contextId: String,
    kind: "status-update",
    status: TaskStatus,
    `final`: Boolean | Null,
    metadata: JsObject | Null,
  )

  case class TaskArtifactUpdateEvent
  (
    taskId: String,
    contextId: String,
    kind: "artifact-update",
    append: Boolean | Null,
    lastChunk: Boolean | Null,
    metadata: JsObject | Null
  )

  case class TaskPushNotificationConfig
  (
    taskId: String,
    pushNotificationConfig: PushNotificationConfig
  )

}

object mcp {

  trait Client {
    def ping(): EmptyResult
    def `sampling/createMessage`(req: CreateMessageRequest): CreateMessageResult
    def `roots/list`(req: ListRootsRequest): ListRootsResult

    def `notifications/cancelled`(ev: CanceledNotification): Unit
    def `notifications/progress`(ev: ProgressNotification): Unit
    def `notifications/message`(ev: LoggingMessageNotification): Unit
    def `notifications/resources/updated`(ev: ResourceUpdatedNotification): Unit
    def `notifications/resources/list_changed`(ev: ResourceListChangedNotification): Unit
    def `notifications/tools/list_changed`(ev: ToolListChangedNotification): Unit
    def `notifications/prompts/list_changed`(ev: PromptListChangedNotification): Unit
  }

  case class CreateMessageRequest()
  case class CreateMessageResult()
  case class ListRootsRequest()
  case class ListRootsResult()
  case class LoggingMessageNotification()
  case class ResourceUpdatedNotification()
  case class ResourceListChangedNotification()
  case class ToolListChangedNotification()
  case class PromptListChangedNotification()


  trait Server {
    def ping(): EmptyResult
    def initialize(req: InitializeRequest): InitializeResult
    def `completion/complete`(req: CompleteRequest): CompleteResult
    def `logging/setLevel`(req: SetLevelRequest): EmptyResult

    def `prompts/list`(req: ListPromptsRequest): ListPromptResult
    def `prompts/get`(req: GetPromptRequest): GetPromptResult

    def `resource/list`(req: ListResourceRequest): ListResourceResult
    def `resources/templates/list`(req: ListResourceTemplateRequest): ListResourceTemplateRequest

    def `resources/read`(req: ReadResourceRequest): ReadResourceResult
    def `resources/subscribe`(req: SubscribeRequest): EmptyResult
    def `resources/unsubscribe`(req: UnsubscribeRequest): EmptyResult

    def `tools/list`(req: ListToolsRequest): ListToolsResult
    def `tools/call`(req: CallToolRequest): CallToolResult

    def `notifications/cancelled`(ev: CanceledNotification): Unit
    def `notifications/progress`(ev: ProgressNotification): Unit
    def `notifications/initialized`(ev: InitializedNotification): Unit
    def `notifications/roots/list_changed`(ev: RootsListChangedNotification): Unit
  }

  case class EmptyResult()
  case class InitializeRequest() // TODO
  case class InitializeResult() // TODO
  case class CompleteRequest()
  case class CompleteResult()
  case class SetLevelRequest()
  case class ListResourceTemplateRequest()
  case class ListPromptsRequest()
  case class ListPromptResult()
  case class GetPromptRequest()
  case class GetPromptResult()
  case class ListResourceRequest()
  case class ListResourceResult()
  case class ReadResourceRequest()
  case class ReadResourceResult()
  case class SubscribeRequest()
  case class UnsubscribeRequest()
  case class ListToolsRequest()
  case class ListToolsResult()
  case class CallToolRequest()
  case class CallToolResult()

  case class CanceledNotification()
  case class ProgressNotification()
  case class InitializedNotification()
  case class RootsListChangedNotification()


}

object `ag-ui` {

  case class RunAgentInput(
    threadId: String,
    runId: String,
    state: JsObject,  // any
    messages: List[Message],
    tools: List[Tool],
    context: List[Context]
  )

  trait Agent {
    def run(input: RunAgentInput): Iterable[Event]  // use SSE stream
  }

  case class Tool(name: String, description: String, parameters: JsObject)  // parameters: JSON Schema define the parameters for the tool
  case class Context(description: String, value:String)

  case class Message
  (
    id: String,
    role: "user" | "assistant" | "system" | "tool" | "developer",
    content: String | Null,
    name: String | Null, // Optional user identifier
    toolCalls: List[ToolCall] | Null, // when role = "assistant"
    toolCallId: String | Null
  )

  case class ToolCall
  (
    id: String,
    `type`: String,
    function: String,
    args: String
  )

  enum Event:
    def timestamp: Long | Null

    case RunStarted(threadId: String, runId: String, timestamp: Long | Null)
    case RunFinished(threadId: String, runId: String, timestamp: Long | Null)
    case RunError(message: String, code: String, timestamp: Long | Null)
    case StepStarted(stepName: String, timestamp: Long | Null)
    case StepFinished(stepName: String, timestamp: Long | Null)
    case TextMessageStart(messageId: String, role: String, timestamp: Long | Null)
    case TextMessageContent(messageId: String, delta: String, timestamp: Long | Null)
    case TextMessageEnd(messageId: String, timestamp: Long | Null)

    case ToolMessageStart(toolCallId: String, toolCallName: String, parentMessageId: String, timestamp: Long | Null)
    case ToolCallArgs(toolCallId: String, delta: JsValue, timestamp: Long | Null)
    case ToolMessageEnd(toolCallId: String, timestamp: Long | Null)

    case StateSnapshot(snapshot: JsObject, timestamp: Long | Null)
    case StateDelta(delta: JsObject, timestamp: Long | Null)
    case MessageSnapshot(messages: List[Message], timestamp: Long | Null)

    case Raw(event: JsObject, source: String, timestamp: Long | Null)
    case Custom(nae: String, value: JsObject, timestamp: Long | Null)

}