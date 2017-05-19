package no.philipp.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerStreamTracer;

public class InterceptorExample {
    public static void main(String[] args) {

        Server server = ServerBuilder
                .forPort(8080)
                // Add interceptor to service
                .addService(ServerInterceptors.intercept(new ExampleService(), new ExampleServerInterceptor()))
                // …or StreamTracerFactory to the whole server
                .addStreamTracerFactory(new ServerStreamTracer.Factory() {
                    @Override
                    public ServerStreamTracer newServerStreamTracer(final String fullMethodName, final Metadata headers) {
                        return new ServerStreamTracer() {
                            // Override methods here
                        };
                    }
                })
                .build();

    }

    public static class ExampleServerInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call, final Metadata headers, final ServerCallHandler<ReqT, RespT> next) {
            // Do things
            return next.startCall(call, headers);
        }
    }

    public static class ExampleClientInterceptor implements ClientInterceptor {
        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(final MethodDescriptor<ReqT, RespT> method, final CallOptions callOptions, final Channel next) {
            // Do things
            return next.newCall(method, callOptions);
        }
    }
}
