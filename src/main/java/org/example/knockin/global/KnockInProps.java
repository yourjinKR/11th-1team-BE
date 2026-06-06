package org.example.knockin.global;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class KnockInProps {
    private String clientUrl;
    private String serverUrl;
    private String clientSuccessUrl;
    private String clientErrorUrl;
    private List<String> corsUrls;
}
