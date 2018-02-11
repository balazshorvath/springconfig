package hu.springconfig.service.authentication;

import hu.springconfig.Application;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdentityServiceTest {
    @MockBean
    private IIdentityRepository identityRepository;
    @Autowired
    private IdentityService identityService;

    @Test
    public void testMockito() {
        Identity identity = new Identity();
        when(identityRepository.findOne(eq(1L))).thenReturn(identity);

        assertEquals(identity, identityService.get(1L));
    }
}
