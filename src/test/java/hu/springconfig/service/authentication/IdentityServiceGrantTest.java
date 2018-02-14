package hu.springconfig.service.authentication;

import hu.springconfig.TestApplication;
import hu.springconfig.TestBase;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdentityServiceGrantTest extends TestBase {
    @MockBean
    private IIdentityRepository identityRepository;
    @MockBean
    private RoleService roleService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private BCryptPasswordEncoder encoder;
}
