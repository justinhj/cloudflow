package taxiride.processor;

import org.apache.flink.fs.shaded.hadoop3.org.apache.hadoop.conf.Configuration;
import org.apache.flink.fs.shaded.hadoop3.org.apache.hadoop.fs.azure.KeyProvider;
import org.apache.flink.fs.shaded.hadoop3.org.apache.hadoop.fs.azure.KeyProviderException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class EnvironmentVariableKeyProvider implements KeyProvider {
    Logger log = LoggerFactory.getLogger(EnvironmentVariableKeyProvider.class);

    public static final String AZURE_STORAGE_KEY_ENV_VARIABLE = "AZURE_STORAGE_KEY";

    @Override
    public String getStorageAccountKey(String s, Configuration configuration) throws KeyProviderException {
        String azureStorageKey = System.getenv(AZURE_STORAGE_KEY_ENV_VARIABLE);

        if (azureStorageKey != null) {
            return azureStorageKey;
        } else {
            throw new KeyProviderException("Unable to retrieve Azure storage key from environment. \""
                    + AZURE_STORAGE_KEY_ENV_VARIABLE + "\" not set.");
        }
    }
}