package com.example.service;

import com.example.data.UserAccountLDAPDetails;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Repository;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Repository
@Slf4j
public class UserAccountsLDAPRepository {

    private static String BASE_DN = "DC=test,DC=oventus,DC=com";

    private Hashtable<String, String> env = new Hashtable<String, String>();

    @Autowired
    private LdapTemplate ldapTemplate;

    /** Password attribute name */
    private String passwordAttributeName = "userPassword";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public UserAccountLDAPDetails getUserAccountLDAPDetailsWithCN(String userCN) {
        logger.info("getUserAccountLDAPDetailsWithCN : {}", userCN);
        UserAccountLDAPDetails userAccountLDAPDetails = null;

        String userDN = findUserDn(userCN, false)
                .orElseThrow(() ->  new RuntimeException("userDN not found"));

        try {
            userAccountLDAPDetails = ldapTemplate.lookup(userDN, new UserAccountAttributesMapper());
            userAccountLDAPDetails.setDn(userDN);

            logger.info("userAccountLDAPDetails = {}", userAccountLDAPDetails);

        } catch (NameNotFoundException e) {
            logger.error(e.getMessage());
        }
        return userAccountLDAPDetails;
    }


    public UserAccountLDAPDetails getUserAccountLDAPDetails(String userId) {
        logger.info("getUserAccountLDAPDetails :" + userId);
        UserAccountLDAPDetails userAccountLDAPDetails = null;

        String userDN = findUserDn(userId, true)
                .orElseThrow(() ->  new RuntimeException("userDN not found"));

        try {
            userAccountLDAPDetails = ldapTemplate.lookup(userDN, new UserAccountAttributesMapper());
            userAccountLDAPDetails.setDn(userDN);

        } catch (NameNotFoundException e) {
            logger.error(e.getMessage());
        }
        return userAccountLDAPDetails;
    }

    //Delete Operations
    //**************************************************************************************
    //**************************************************************************************
    //**************************************************************************************
    //**************************************************************************************

    public void delete(UserAccountLDAPDetails user) {
        Name dn = LdapNameBuilder
                .newInstance()
                .add("dc","com")
                .add("dc","oventus")
                .add("dc","test")
                .add("ou", "Product Development")
                //.add("uid",user.getUid())          //delete based on uid
                .add("cn", user.getFirstname())
                .build();
        DirContextAdapter context = new DirContextAdapter(dn);
        context.setAttributeValues("objectclass", new String[]{"top", "person", "organizationalPerson", "inetOrgPerson"});
        context.setAttributeValue("cn", user.getUsername());
        context.setAttributeValue("givenName", user.getFirstname());
        context.setAttributeValue("sn", user.getSurname());
        context.setAttributeValue("uid", user.getUserId());

        //Dummy password
        context.setAttributeValue("userpassword", "3n9qHJK*~/H.fT");
        ldapTemplate.unbind(dn);
    }

    private class UserAccountAttributesMapper implements AttributesMapper<UserAccountLDAPDetails> {
        @Override
        public UserAccountLDAPDetails mapFromAttributes(Attributes attributes) throws NamingException {
            UserAccountLDAPDetails userAccountLDAPDetails = new UserAccountLDAPDetails();

            getAttribute(attributes, "uid")
                    .ifPresent(s -> userAccountLDAPDetails.setUserId(s));

            getAttribute(attributes, "cn")
                    .ifPresent(s -> userAccountLDAPDetails.setUsername(s));

            return userAccountLDAPDetails;
        }
    }

    //Create Operations
    //**************************************************************************************
    //**************************************************************************************
    //**************************************************************************************

    public void createUser(UserAccountLDAPDetails userAccountLDAPDetails, String contextLocation) {
        Name userDn = buildDn(userAccountLDAPDetails, contextLocation);
        logger.info("userDn = {}", userDn.toString());
        DirContextAdapter context = new DirContextAdapter(userDn);
        mapToContext(userAccountLDAPDetails, context);
        ldapTemplate.bind(context);
    }

    protected Name buildDn(UserAccountLDAPDetails userAccountLDAPDetails, String parentDn) {
        String parentContext = parentDn.substring(parentDn.indexOf("ou="));
        logger.info("parentContext = {}", parentContext);
        return buildDn(userAccountLDAPDetails.getUsername(), parentContext);
    }

    protected Name buildDn(String fullname, String parentContext) {
        LdapNameBuilder builder = LdapNameBuilder.newInstance();
        builder.add(parentContext);
        builder.add("cn", fullname);
        return builder.build();
    }

    protected void mapToContext(UserAccountLDAPDetails userAccountLDAPDetails, DirContextOperations context) {
        //context.setAttributeValues("objectclass", new String[] {"user", "top", "organizationalPerson", "person", "ndsLoginProperties"});
        context.setAttributeValues("objectclass", new String[] {"top", "person", "organizationalPerson", "inetOrgPerson"});
        context.setAttributeValue("cn", userAccountLDAPDetails.getUsername());

        context.setAttributeValue("givenName", userAccountLDAPDetails.getFirstname());
        context.setAttributeValue("sn", userAccountLDAPDetails.getSurname());
        context.setAttributeValue("uid", userAccountLDAPDetails.getUserId());

        //Dummy password
        context.setAttributeValue("userpassword", "3n9qHJK*~/H.fT");
    }

    //Update Operations
    //**************************************************************************************
    //**************************************************************************************
    //**************************************************************************************
    //**************************************************************************************


    public void modifyUsername(String userid, String username) {
        UserAccountLDAPDetails userAccountLDAPDetails = getUserAccountLDAPDetails(userid);
        System.out.println("userAccountLDAPDetails = " + userAccountLDAPDetails);

        if (userAccountLDAPDetails == null || userAccountLDAPDetails.getDn() == null)
            throw new RuntimeException("Modify Username Operation Failed");

        LdapName ldapName = LdapNameBuilder.newInstance(userAccountLDAPDetails.getDn()).build();
        String ldapNameString = ldapName.toString();
        System.out.println(ldapNameString);

        LdapNameBuilder ldapNameBuilderNew = LdapNameBuilder.newInstance();
        ldapNameBuilderNew.add(ldapNameString.substring(ldapNameString.indexOf("ou=")));
        ldapNameBuilderNew.add("cn", username);
        String ldapNameNewString = ldapNameBuilderNew.build().toString();

        ldapTemplate.rename(ldapNameString, ldapNameNewString);

    }

    public void modifyPassword(String userId, String newPassword) {
        UserAccountLDAPDetails userAccountLDAPDetails = getUserAccountLDAPDetails(userId);
        logger.info("Changing password for user: " + userAccountLDAPDetails.getDn());

        final ModificationItem[] passwordChange = new ModificationItem[] {
                new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(passwordAttributeName, newPassword)) };

        ldapTemplate.modifyAttributes(userAccountLDAPDetails.getDn(), passwordChange);
    }

    public void modifyAttribute(String dn, String attributeName, String attributeData) {
        logger.info("modifyAttribute: dn = " + dn + ", attributeName = " + attributeName + ", attributeData = " + attributeData);
        Attribute attr = new BasicAttribute(attributeName, attributeData);
        ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);
        ldapTemplate.modifyAttributes(dn, new ModificationItem[] {item} );
    }

    public void modifyMultiAttribute(String dn, String attributeName, List<String> attributeDataList) {
        logger.info("modifyAttribute: dn = " + dn + ", attributeName = " + attributeName + ", attributeDataList = " + attributeDataList);

        Attributes myAttrs = new BasicAttributes(true);
        Attribute myAttr = new BasicAttribute(attributeName);
        for (String attributeData : attributeDataList)
            myAttr.add(attributeData);

        myAttrs.put(myAttr);

        ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, myAttr);
        ldapTemplate.modifyAttributes(dn, new ModificationItem[] {item} );
    }


    private Optional<String> findUserDn(String userIdentifier, boolean useridBasedFind) {
        String searchAttr = useridBasedFind?"userid":"cn";
        logger.info("findUserDn: {} = {}", searchAttr, userIdentifier);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        LdapQuery query = query()
                .base(BASE_DN)
                .where(searchAttr)
                .is(userIdentifier);

        String dn = ldapTemplate.searchForObject(query, ctx -> {
            DirContextOperations adapter = (DirContextOperations) ctx;
            return adapter.getNameInNamespace();
        });
        logger.info("findUserDn: dn = {}", dn);

        return (dn == null)? Optional.empty(): Optional.of(dn);
    }


    private Optional<String> getAttribute(Attributes attrs, String attrName) {
        try {
            String retval = (String) attrs.get(attrName).get();
            return Optional.of(retval);
        } catch (NullPointerException e) {
            logger.debug(attrName + " is " + e.getMessage());
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }

    private List<String> getAttributeList(Attributes attrs, String name) throws NamingException {
        Attribute attr = attrs.get(name);
        List<String> retval = new ArrayList<>();
        if (attr != null) {
            int size = attr.size();
            for (int i = 0; i < size; i++)
                retval.add((String) attr.get(i));
        }
        return retval;
    }

    //Search Operations
    //**************************************************************************************
    //**************************************************************************************
    //**************************************************************************************
    //**************************************************************************************

    public UserAccountLDAPDetails search(UserAccountLDAPDetails user) {
        try {

            DirContext ctx = new InitialDirContext(env);
            String base = "ou=Product Development,dc=test,dc=oventus,dc=com";

            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

            String filter = "(&(objectclass=person)(givenName="+user.getFirstname()+"))";

            NamingEnumeration results = ctx.search(base, filter, sc);

            while (results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                Attributes attrs = sr.getAttributes();

                if(attrs != null)
                    log.info("record found "+attrs);
            }
            ctx.close();

            return user;
        } catch (Exception e) {
            return user;
        }
    }
    public UserAccountsLDAPRepository() {

            try {
                env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                env.put(Context.PROVIDER_URL, "ldap://localhost:389");
            } catch (Exception e) {
                //log.error(e, e);
        }
    }
}