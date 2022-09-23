package com.mickhu.ldap.controllers;

import com.mickhu.ldap.exceptions.IncorrectParamsException;
import com.mickhu.ldap.exceptions.UnSupportedActionTypeException;
import com.mickhu.ldap.exceptions.UnSupportedGadgetTypeException;
import com.mickhu.ldap.exceptions.UnSupportedPayloadTypeException;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;

public interface LdapController {
    void sendResult(InMemoryInterceptedSearchResult result, String base) throws Exception;
    void process(String base) throws UnSupportedPayloadTypeException, IncorrectParamsException, UnSupportedGadgetTypeException, UnSupportedActionTypeException;
}
