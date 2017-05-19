
# Protocol buffers
## Message definition
```protobuf
message SomeRequestObject {
  int32 x = 1;
  int32 ys = 2;
  string label = 3;
}
```

## Generated message
```java
SomeResponseObject.newBuilder()
  .setSomeString("Hello world!")
  .setSomeInt(42)
  .setSomeBoolean(true)
  …
  .build();
# Returns immutable message object
```

## Service definition
```protobuf
service SomeService {
  rpc someUnaryMethod(SomeRequestObject) returns (SomeResponseObject);
  rpc someServerStreamingMethod(SomeRequestObject) returns (stream SomeResponseObject);
  rpc someBiStreamingMethod(stream SomeRequestObject) returns (stream SomeResponseObject);
}
```

# GRPC

## Service
```java
public class SomeService extends SomeServiceImplBase {
  @Override
  public void someServerStreamingMethod(
    final SomeRequestObject request,
    final StreamObserver responseObserver) {
    // Stream messages to the client
    while(someCondition) {
      responseObserver.onNext(responseObject);
    }
    // Close the stream
    responseObserver.onCompleted();
  }
}
```

## Stub
```java
SomeServiceFutureStub stub = SomeServiceGrpc.newFutureStub(channel);
ListenableFuture futureResponse = stub.someUnaryMethod(someGeneratedRequestObject);

SomeServiceBlockingStub stub = SomeServiceGrpc.newBlockingStub(channel);
Iterator responseStream = stub.someServerStreamMethod(someGeneratedRequestObject);
```

## Channel
```java
Channel channel = ManagedChannelBuilder
  .forAddress("localhost", 8080)
  .usePlaintext(true)
  .build();
```

## Interceptor
```java
public class SomeInterceptor extends ServerInterceptor {
  @Override
  public ServerCall.Listener interceptCall(
      ServerCall call,
      Metadata headersFromClient,
      ServerCallHandler next) {
    // Some interception logic here:
    // I.e. monitor, rewrite, or retry calls
  }
}
```

## Context
```java
// Define the context key
public static final Context.Key REQUEST_ID = Context.key("REQUEST_ID");
```

```java
// Enter the context
Context context = Context.current().withValue(ApiUtil.REQUEST_ID, requestId);
Context previous = context.attach();
try {
 // do stuff inside context…
} finally {
 context.detach(previous);
}
```
```java
// Use the context value inside the context
} catch (IOExeption e)
 String requestId = ApiUtil.REQUEST_ID.get();
 LOG.error("Request id: " + requestId, e);
}
```

## Metadata
```java
AtomicReference<Metadata> trailersCapturer = new AtomicReference<>();
CubeServiceBlockingStub cubeClient = MetadataUtils.captureMetadata(stub, headersCapturer, trailersCapturer);
ExecuteResponse response = cubeClient.execute(request).get();
String cpuUsage = trailersCapturer.get().get(GrpcMonitoring.KEY_CPU_TIME);
```

## Deadlines

```java
// Client-side
ExecuteResponse response = stub
  .withDeadline(Deadline.after(5, TimeUnit.SECONDS))
  .execute(request)
  .get();
```

```java
// Server-side
try {
  while (!context.isCancelled()) {
    // do work
  }
} finally {
  context.detach(previous);
}
```

## Load Balancer
```java
channel.loadBalancerFactory(
  RoundRobinLoadBalancerFactory.getInstance()
);
```

## GRPC CLI
```java
Server server = ServerBuilder
  .forPort(8080)
  <mark>.addService(ProtoReflectionService.newInstance())</mark>
  .addService(new SomeService())
  .build();
```

```bash
$ grpc call localhost:8080 test.SomeService.Greet "name: 'andy', text: 'Hello server!'"
connecting to localhost:8080
name: "Server"
text: "Hello andy"

Rpc succeeded with OK status
```
