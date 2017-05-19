package no.philipp.grpc;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import no.philipp.proto.Example.ExampleMessage;
import no.philipp.proto.ExampleServiceGrpc;

public class ExampleClient {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        int n = 10;
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        System.out.println("Starting client...");
        Channel channel = ManagedChannelBuilder
                .forAddress("localhost", port)
                .usePlaintext(true)
                .build();

        ExampleMessage message = ExampleMessage.newBuilder().setNumber(0).build();

        // Unary blocking
        ExampleServiceGrpc.ExampleServiceBlockingStub blockingStub = ExampleServiceGrpc.newBlockingStub(channel);
        blockingStub.unaryMethod(message);

        // Unary future
        ExampleServiceGrpc.ExampleServiceFutureStub futureStub = ExampleServiceGrpc.newFutureStub(channel);
        Future future = futureStub.unaryMethod(message);
        future.get(10, TimeUnit.SECONDS);

        // Server streaming
        ExampleServiceGrpc.ExampleServiceStub stub = ExampleServiceGrpc.newStub(channel);
        stub.serverStreamMethod(message, new StreamObserver<ExampleMessage>() {
            @Override
            public void onNext(final ExampleMessage responseMessage) {
                System.out.printf("Received message from server %d \n", responseMessage.getNumber());
            }

            @Override
            public void onError(final Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.printf("Stream ended.");
            }
        });

        // Client streaming
        StreamObserver streamObserver = stub.clientStreamMethod(new StreamObserver<ExampleMessage>() {
            @Override
            public void onNext(final ExampleMessage value) {

            }

            @Override
            public void onError(final Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

        System.out.printf("Sending messages to port %d \n", port);
        for (int i = 0; i < n; i++) {
            Thread.sleep(2_000);
            message = ExampleMessage.newBuilder().setNumber(i).build();
            streamObserver.onNext(message);
            System.out.printf("Sent message: %d \n", i);
        }
        streamObserver.onCompleted();

        // Bidirectional streaming
        streamObserver = stub.bidirectionalMethod(new StreamObserver<ExampleMessage>() {
            @Override
            public void onNext(final ExampleMessage value) {

            }

            @Override
            public void onError(final Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

        System.out.printf("Sending messages to port %d \n", port);
        for (int i = 0; i < n; i++) {
            Thread.sleep(2_000);
            message = ExampleMessage.newBuilder().setNumber(i).build();
            streamObserver.onNext(message);
            System.out.printf("Sent message: %d \n", i);
        }
        streamObserver.onCompleted();
    }
}
