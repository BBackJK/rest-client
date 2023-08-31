package bback.module.http.bean.mapper;

import bback.module.http.exceptions.RestClientDataMappingException;
import bback.module.http.interfaces.ResponseMapper;
import bback.module.http.util.RestClientClassUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

@Component
public class DefaultResponseMapper implements ResponseMapper {
    private final ObjectMapper om;

    public DefaultResponseMapper() {
        this.om = new ObjectMapper();
    }

    @Override
    public <T> T convert(String value, Class<T> clazz) throws RestClientDataMappingException {
        try {
            if (!clazz.isInterface() && !RestClientClassUtils.isPrimitiveOrString(clazz)) {
                this.canConvert(clazz);
            }
            return this.om.readValue(value, clazz);
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new RestClientDataMappingException(e);
        }
    }

    @Override
    public <T, E> T convert(String value, Class<T> genericClass, Class<E> rawClass) throws RestClientDataMappingException {
        try {
            if (!genericClass.isInterface() && !RestClientClassUtils.isPrimitiveOrString(genericClass)) {
                this.canConvert(genericClass);
            }

            if (!rawClass.isInterface() && !RestClientClassUtils.isPrimitiveOrString(rawClass)) {
                this.canConvert(rawClass);
            }

            JavaType javaType = this.om.getTypeFactory().constructParametricType(genericClass, rawClass);
            return this.om.readValue(value, javaType);
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new RestClientDataMappingException(e);
        }
    }

    @Override
    public <T, E> E convert(T value, Class<E> clazz) throws RestClientDataMappingException {
        try {
            if (!clazz.isInterface() && !RestClientClassUtils.isPrimitiveOrString(clazz)) {
                this.canConvert(value.getClass());
                this.canConvert(clazz);
            }
            return this.om.convertValue(value, new TypeReference<E>() {});
        } catch (IllegalArgumentException e) {
            throw new RestClientDataMappingException(e);
        }
    }

    @Override
    public <T> T toXml(String value, Class<T> clazz) throws RestClientDataMappingException {
        try {
            if (!clazz.isInterface() && !RestClientClassUtils.isPrimitiveOrString(clazz)) {
                this.canConvert(clazz);
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new StringReader(value));
        } catch (JAXBException e) {
            throw new RestClientDataMappingException(e);
        }
    }
}
