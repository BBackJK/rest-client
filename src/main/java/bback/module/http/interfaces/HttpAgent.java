package bback.module.http.interfaces;

import bback.module.http.exceptions.RestClientCallException;
import bback.module.http.wrapper.RequestMetadata;
import bback.module.http.wrapper.ResponseMetadata;

public interface HttpAgent {

    ResponseMetadata doGet(RequestMetadata requestMetadata) throws RestClientCallException;
    ResponseMetadata doPost(RequestMetadata requestMetadata) throws RestClientCallException;
    ResponseMetadata doPatch(RequestMetadata requestMetadata) throws RestClientCallException;
    ResponseMetadata doPut(RequestMetadata requestMetadata) throws RestClientCallException;
    ResponseMetadata doDelete(RequestMetadata requestMetadata) throws RestClientCallException;

}
