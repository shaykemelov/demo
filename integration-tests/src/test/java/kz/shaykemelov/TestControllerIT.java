package kz.shaykemelov;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@Testcontainers
public class TestControllerIT
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestControllerIT.class);

    private static final DockerImageName APP = DockerImageName.parse(
            "demo-application:0.0.1-SNAPSHOT"
    );

    public static final DockerImageName MOCKSERVER_IMAGE = DockerImageName
            .parse("mockserver/mockserver")
            .withTag("mockserver-" + MockServerClient.class.getPackage().getImplementationVersion());

    private Network network = Network.newNetwork();

    public MockServerContainer mockServer;

    public GenericContainer<?> app;

    // mockServer
    private String mockServerHost;

    private int mockServerPort;

    // app
    private String appHost;

    private int appPort;

    @BeforeEach
    void beforeEach()
    {
        mockServer = new MockServerContainer(MOCKSERVER_IMAGE)
                .withNetwork(network)
                .withNetworkAliases("mockServer")
                .withLogConsumer(new Slf4jLogConsumer(LOGGER));

        mockServer.start();

        mockServerHost = mockServer.getHost();
        mockServerPort = mockServer.getFirstMappedPort();

        app = new GenericContainer<>(APP)
                .withNetwork(network)
                .withNetworkAliases("app")
                .withExposedPorts(8080)
                .withFileSystemBind("./target/jacoco-agent", "/jacoco-agent", BindMode.READ_WRITE)
                .withFileSystemBind("../jacoco-report/target/jacoco-report", "/jacoco-report", BindMode.READ_WRITE)
                .withEnv("JAVA_OPTS", "-javaagent:/jacoco-agent/org.jacoco.agent-runtime.jar=destfile=/jacoco-report/jacoco-it.exec,append=true")
                .withEnv("TEST_BASE_URL", "http://mockServer:1080")
                .withLogConsumer(new Slf4jLogConsumer(LOGGER));

        app.start();

        appHost = app.getHost();
        appPort = app.getFirstMappedPort();
    }

    @AfterEach
    void afterEach()
    {
        try (final var stopExec = app.getDockerClient().stopContainerCmd(app.getContainerId()))
        {
            stopExec.exec();
        }

        network.close();
    }

    @Test
    void testCreateInteraction() throws URISyntaxException, IOException, InterruptedException
    {
        // prepare
        final var mockServerClient = new MockServerClient(mockServerHost, mockServerPort);

        final String expectedBody = UUID.randomUUID().toString();
        mockServerClient
                .when(request("/"))
                .respond(response().withBody(expectedBody));

        final java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .GET()
                .uri(new URI("http://" + appHost + ":" + appPort + "/google"))
                .build();

        final HttpClient httpClient = HttpClient.newHttpClient();

        // do
        final HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String actualBody = httpResponse.body();

        // assert & verify
        assert expectedBody.equals(actualBody);
    }
}
