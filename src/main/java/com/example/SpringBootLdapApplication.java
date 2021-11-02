package com.example;

import com.example.data.UserAccountLDAPDetails;
import com.example.service.UserAccountsLDAPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.naming.NamingEnumeration;
import javax.naming.directory.*;

@SpringBootApplication
public class SpringBootLdapApplication implements ApplicationRunner {

	private static Logger log = LoggerFactory.getLogger(SpringBootLdapApplication.class);

	private final UserAccountsLDAPRepository userAccountsLDAPRepository;
	//cn=Administrator,cn=users,dc=activedirectory,dc=oventus,dc=com
	private String BASE_DN = "DC=test,DC=oventus,DC=com";

	public SpringBootLdapApplication(UserAccountsLDAPRepository userAccountsLDAPRepository) {
		this.userAccountsLDAPRepository = userAccountsLDAPRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBootLdapApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		//CREATE
		/*createUser("a", "ab", "56099781", "Accounting");
		createUser("Sachin", "Nikam", "56099782", "Product Development");*/
		//createUser("manoj1", "yadav", "56099783", "Accounting");

		//UPDATE
		//userAccountsLDAPRepository.modifyUsername("56099783", "Danielle Ashford");
		//userAccountsLDAPRepository.modifyAttribute("cn=Danielle Ashford,ou=Management,dc=test,dc=oventus,dc=com", "pager", "813769");

		//SEARCH
		//UserAccountLDAPDetails user = new UserAccountLDAPDetails();
		//user.setFirstname("Manoj");
		//UserAccountsLDAPRepository userAccountsLDAPRepository = new UserAccountsLDAPRepository();
		//userAccountsLDAPRepository.search(user);

		//DELETE
		UserAccountLDAPDetails user = new UserAccountLDAPDetails("","","","manoj","");
		userAccountsLDAPRepository.delete(user);
	}

	private void createUser(String fn, String sn, String userId, String department) {
		UserAccountLDAPDetails userAccountLDAPDetails = new UserAccountLDAPDetails();
		userAccountLDAPDetails.setUsername(fn + " " + sn);
		userAccountLDAPDetails.setFirstname(fn);
		userAccountLDAPDetails.setSurname(sn);
		userAccountLDAPDetails.setUserId(userId);

		userAccountsLDAPRepository.createUser(userAccountLDAPDetails, "ou=" + department + "," + BASE_DN);
	}
}
