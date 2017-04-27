package org.talend.daikon.hystrix;

import org.apache.http.client.methods.HttpRequestBase;

public interface HttpSecurityProvider {

    void addSecurityHeaders(HttpRequestBase request);

}
