import com.ous.aethererp.entity.RoleEntity;
import com.ous.aethererp.repo.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepo;

    @Override
    public void run(String... args) throws Exception {

        if (roleRepo.findByName("ROLE_USER").isEmpty()){
            roleRepo.save(
                    RoleEntity.builder()
                            .name("ROLE_USER")
                            .build());
        }

        if (roleRepo.findByName("ROLE_ADMIN").isEmpty()){
            roleRepo.save(
                    RoleEntity.builder()
                            .name("ROLE_ADMIN")
                            .build());
        }
    }
}
