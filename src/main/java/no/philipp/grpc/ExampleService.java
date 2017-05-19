package no.philipp.grpc;

import java.io.IOException;

import io.grpc.Context;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;
import no.philipp.proto.Example.ExampleMessage;
import no.philipp.proto.ExampleServiceGrpc;

/**
 *
 */
public class ExampleService extends ExampleServiceGrpc.ExampleServiceImplBase {

    @Override
    public void unaryMethod(final ExampleMessage message, final StreamObserver<ExampleMessage> responseObserver) {
        System.out.printf("Received message: %d \n", message.getNumber());
        responseObserver.onNext(ExampleMessage.newBuilder().setNumber(42).build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ExampleMessage> clientStreamMethod(final StreamObserver<ExampleMessage> responseObserver) {
        return new StreamObserver<ExampleMessage>() {
            @Override
            public void onNext(final ExampleMessage message) {
                System.out.printf("Received message: %d \n", message.getNumber());
            }

            @Override
            public void onError(final Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void serverStreamMethod(final ExampleMessage request, final StreamObserver<ExampleMessage> responseObserver) {
        System.out.printf("Received message: %d \n", request.getNumber());
        int i = 0;
        while (i < 42 && Context.current().isCancelled()) {
            responseObserver.onNext(ExampleMessage.newBuilder().setNumber(i++).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ExampleMessage> bidirectionalMethod(final StreamObserver<ExampleMessage> responseObserver) {
        return new StreamObserver<ExampleMessage>() {
            @Override
            public void onNext(final ExampleMessage value) {
                responseObserver.onNext(value.toBuilder().setNumber(value.getNumber() + 1).build());
            }

            @Override
            public void onError(final Throwable t) {
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Starting server...");
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        Server server = ServerBuilder
                .forPort(port)
                .addService(new ExampleService())
                .addService(ProtoReflectionService.newInstance())
                .build();
        server.start();
        System.out.println(String.format("Server running on port %d.", port));
        server.awaitTermination();
    }
}
