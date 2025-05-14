package samplesftpclients;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import samplesftpclients.util.EnvConfig;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MinaTest {


    private Mina mina;

    @Disabled
    @Test
    void connect() {
        //given
        mina = Mina.keyLogin(EnvConfig.USERNAME, Path.of(EnvConfig.PRIVATE_KEY_PATH), EnvConfig.HOST, EnvConfig.PORT);

        // when then
        assertDoesNotThrow(() -> mina.connect());
    }
}