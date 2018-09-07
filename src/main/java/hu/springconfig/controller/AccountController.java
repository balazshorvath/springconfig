package hu.springconfig.controller;

import hu.springconfig.config.message.entity.AccountMessages;
import hu.springconfig.config.message.entity.InviteMessages;
import hu.springconfig.data.dto.account.AccountCreate;
import hu.springconfig.data.dto.account.AccountDTO;
import hu.springconfig.data.dto.account.AccountUpdate;
import hu.springconfig.data.dto.account.InviteCreate;
import hu.springconfig.data.dto.authentication.identity.IdentityCreate;
import hu.springconfig.data.dto.simple.OKResponse;
import hu.springconfig.data.entity.Account;
import hu.springconfig.data.entity.Invite;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.query.model.Condition;
import hu.springconfig.exception.ForbiddenException;
import hu.springconfig.exception.ResponseException;
import hu.springconfig.service.account.AccountService;
import hu.springconfig.service.account.InviteService;
import hu.springconfig.service.authentication.IdentityService;
import hu.springconfig.service.mail.MailingService;
import hu.springconfig.service.meal.MealService;
import hu.springconfig.util.Util;
import hu.springconfig.validator.request.ConditionValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@RestController
@Transactional
public class AccountController {
    @Autowired
    private InviteService inviteService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private MealService mealService;
    @Autowired
    private MailingService mailingService;
    private ModelMapper modelMapper;
    @Autowired
    private ConditionValidator conditionValidator;


    @PreAuthorize("hasAuthority('IDENTITY_GET') || @identityAuthorization.isSelf(authentication, #id)")
    @GetMapping("/account/{id}")
    public AccountDTO get(@PathVariable Long id) {
        return modelMapper.map(accountService.get(id), AccountDTO.class);
    }

    @PreAuthorize("hasAuthority('IDENTITY_UPDATE') || @identityAuthorization.isSelf(authentication, #id)")
    @PutMapping("/account/{id}")
    public AccountDTO update(@PathVariable Long id, @RequestBody AccountUpdate account, Authentication authentication) {
        return modelMapper.map(
                accountService.update(
                        id,
                        account.getFirstName(),
                        account.getLastName(),
                        account.getDailyCalorieGoal(),
                        account.getVersion(),
                        (Identity) authentication.getPrincipal()
                ),
                AccountDTO.class
        );
    }

    @PostMapping("/account/{verificationCode}/verify")
    public OKResponse verify(@PathVariable String verificationCode) {
        identityService.verify(verificationCode);
        return new OKResponse();
    }

    @PostMapping("/account/register")
    public OKResponse register(@RequestBody AccountCreate accountCreate) {
        IdentityCreate identityCreate = accountCreate.getIdentity();
        String verificationCode = Util.randomString(Util.CHAR_AND_NUMBER_POOL, 16);
        Identity identity = identityService.createIdentity(
                identityCreate.getEmail(),
                identityCreate.getPassword(),
                identityCreate.getPasswordConfirm(),
                verificationCode
        );
        accountService.create(
                identity,
                accountCreate.getFirstName(),
                accountCreate.getLastName(),
                accountCreate.getDailyCalorieGoal()
        );
        mailingService.sendVerification(identity.getEmail(), accountCreate.getFirstName(), verificationCode);
        return new OKResponse();
    }

    /**
     * Create only the Account for a specified identity.
     * Fails, if the identity already has an account attached.
     *
     * @param accountCreate
     * @return
     */
    @PreAuthorize("hasAuthority('IDENTITY_UPDATE') || @identityAuthorization.isSelf(authentication, #identityId)")
    @PostMapping("/account/register/{identityId}")
    public OKResponse register(@PathVariable Long identityId, @RequestBody AccountCreate accountCreate,
                               Authentication authentication) {
        Identity identity = identityService.get(identityId);
        if (identity.getAccount() != null) {
            throw new ForbiddenException(AccountMessages.ACCOUNT_REGISTRATION_ALREADY_EXISTS);
        }
        accountService.create(
                identity,
                accountCreate.getFirstName(),
                accountCreate.getLastName(),
                accountCreate.getDailyCalorieGoal()
        );
        return new OKResponse();
    }

    @PreAuthorize("hasAuthority('IDENTITY_DELETE')")
    @DeleteMapping("/account/{id}")
    public OKResponse delete(@PathVariable Long id, Authentication authentication) {
        Account account = accountService.get(id);
        mealService.deleteForAccount(account);
        accountService.delete(id);
        identityService.delete((Identity) authentication.getPrincipal(), id);
        return new OKResponse();
    }

    @PreAuthorize("hasAuthority('IDENTITY_LIST')")
    @PostMapping("/account/list")
    public Page<AccountDTO> listAccounts(@RequestBody(required = false) Condition condition, Pageable pageable) {
        conditionValidator.validate(condition);
        return accountService.list(condition, pageable).map(account -> modelMapper.map(account, AccountDTO.class));
    }


    /* Invite entries*/

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/account/invite/{id}")
    public Invite getInvite(@PathVariable Long id) {
        return inviteService.get(id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/account/invite")
    public Invite create(@RequestBody InviteCreate create) {
        return inviteService.create(create.getEmail());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/account/invite/{id}")
    public OKResponse deleteInvite(@PathVariable Long id) {
        inviteService.delete(id);
        return new OKResponse();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/account/invite/list")
    public Page<Invite> listInvites(@RequestBody(required = false) Condition condition, Pageable pageable) {
        conditionValidator.validate(condition);
        return inviteService.list(condition, pageable);
    }

    /**
     * Registration without verification. That already happened when the invite key was received.
     * Ignores the email in the request, uses the one in the invite.
     *
     * @param accountCreate
     * @param inviteKey
     * @return
     */
    @PostMapping("/account/invite/{inviteKey}/accept")
    public OKResponse acceptInvite(@RequestBody AccountCreate accountCreate, @PathVariable String inviteKey) {
        IdentityCreate identityCreate = accountCreate.getIdentity();
        Invite invite = inviteService.getByKey(inviteKey);
        if (invite.isUsed()) {
            throw new ResponseException(InviteMessages.INVITE_USED, HttpStatus.CONFLICT);
        }
        Identity identity = identityService.createIdentity(
                invite.getEmail(),
                identityCreate.getPassword(),
                identityCreate.getPasswordConfirm()
        );
        accountService.create(
                identity,
                accountCreate.getFirstName(),
                accountCreate.getLastName(),
                accountCreate.getDailyCalorieGoal()
        );
        invite.setUsed(true);
        inviteService.save(invite);

        return new OKResponse();
    }

    @Autowired
    public void setModelMapper(ModelMapper modelMapper) {
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        this.modelMapper = modelMapper;
    }
}
