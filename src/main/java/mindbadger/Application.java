package mindbadger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication(scanBasePackages="**/mindbadger/**/*")
public class Application {

//    @SuppressWarnings("deprecation")
//	@RequestMapping("/resources-info")
//    public Map<?, ?> getResources() {
//        Map<String, String> result = new HashMap<>();
//        // Add all resources (i.e. Project and Task)
//        for (RegistryEntry entry : resourceRegistry.getResources()) {
//            result.put(entry.getResourceInformation().getResourceType(),
//                resourceRegistry.getResourceUrl(entry.getResourceInformation()));
//        }
//        return result;
//    }
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
