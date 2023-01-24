package kz.shaykemelov.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class TestServiceTest
{
    private final TestService testService = new TestService();

    @Test
    void testTest()
    {
        // prepare
        final String arg = UUID.randomUUID().toString();

        // do
        final String actual = testService.test(arg);

        // assert & verify
        assertEquals(arg, actual);
    }
}