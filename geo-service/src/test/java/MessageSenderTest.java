import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import ru.netology.entity.Country;
import ru.netology.entity.Location;
import ru.netology.geo.GeoService;
import ru.netology.geo.GeoServiceImpl;
import ru.netology.i18n.LocalizationService;
import ru.netology.i18n.LocalizationServiceImpl;
import ru.netology.sender.MessageSenderImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MessageSenderTest {
    GeoService geoServiceImpl;
    LocalizationService localizationServiceImpl;

    @BeforeEach
    public void init() {
        geoServiceImpl = new GeoServiceImpl();
        localizationServiceImpl = new LocalizationServiceImpl();
        System.out.println("Тест запущен");
    }

    @AfterEach
    public void finishedEach() {
        geoServiceImpl = null;
        localizationServiceImpl = null;
        System.out.println("Тест закончен");
    }

    @BeforeAll
    public static void  startedAll() {
        System.out.println("Тесты запущены");
    }
    @AfterAll
    public static void finishedAll() {
        System.out.println("Тесты выполнены");
    }

    @Test
    public void LocationByCoordinatesTest() throws RuntimeException{

        Throwable thrown = assertThrows(RuntimeException.class, () -> {
            geoServiceImpl.byCoordinates(28.4, 105.1);
        });

    }

    @ParameterizedTest
    @MethodSource("source")
    public void GeoServiceImplTest(String ip, Location expected) {
        Location location = geoServiceImpl.byIp(ip);
        assertEquals(expected.getCity(), location.getCity());
        assertEquals(expected.getCountry(), location.getCountry());
        assertEquals(expected.getStreet(), location.getStreet());
        assertEquals(expected.getBuiling(), location.getBuiling());

    }

    private static Stream<Arguments> source() {
        return Stream.of(Arguments.of("127.0.0.1", new Location(null, null, null, 0)),
                Arguments.of("172.0.32.11", new Location("Moscow", Country.RUSSIA, "Lenina", 15)),
                Arguments.of("96.44.183.149", new Location("New York", Country.USA, " 10th Avenue", 32)));
    }

    @ParameterizedTest
    @MethodSource("source2")
    public void localizationTest(Country country, String hi) {
        LocalizationServiceImpl localizationService = new LocalizationServiceImpl();
        String result = localizationService.locale(country);
        assertEquals(hi, result);

    }

    private static Stream<Arguments> source2() {
        return Stream.of(Arguments.of(Country.USA, "Welcome"),
                Arguments.of(Country.RUSSIA, "Добро пожаловать"),
                Arguments.of(Country.BRAZIL, "Welcome"),
                Arguments.of(Country.GERMANY, "Welcome"));
    }

    @Test
    public void messageSenderRussianScenarioTest() {
        Map<String, String> map = new HashMap<>();
        map.put("x-real-ip", "172.0.32.11");

        GeoService geoService = Mockito.mock(GeoService.class);
        Mockito.when(geoService.byIp("172.0.32.11")).thenReturn(new Location("Moscow", Country.RUSSIA, "Lenina", 15));

        LocalizationService localizationService = Mockito.mock(LocalizationService.class);
        Mockito.when(localizationService.locale(Country.RUSSIA)).thenReturn("Добро пожаловать");

        MessageSenderImpl messageSender = new MessageSenderImpl(geoService, localizationService);
        String result = messageSender.send(map);

        assertEquals("Добро пожаловать", result);

    }

    @Test
    public void messageSenderEnglishScenarioTest() {
        Map<String, String> map = new HashMap<>();
        map.put("x-real-ip", "96.44.183.149");

        GeoService geoService = Mockito.mock(GeoService.class);
        Mockito.when(geoService.byIp("96.44.183.149")).thenReturn(new Location("New York", Country.USA, " 10th Avenue", 32));

        LocalizationService localizationService = Mockito.mock(LocalizationService.class);
        Mockito.when(localizationService.locale(Country.USA)).thenReturn("Welcome");

        MessageSenderImpl messageSender = new MessageSenderImpl(geoService, localizationService);
        String res = messageSender.send(map);

        assertEquals("Welcome", res);

    }
}
