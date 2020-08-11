import io.github.wechaty.memorycard.MemoryCard;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MemoryCardTest {

    @Test
    public void testLoad() throws ExecutionException, InterruptedException {
        MemoryCard card = new MemoryCard("default", null);
        card.load();
        Map<String, Object> map = (Map<String, Object>) card.get("person");
        if (map != null) {
            map.forEach((key, value) -> {
                System.out.println(key + ":" + value);
            });
        }
    }
}
