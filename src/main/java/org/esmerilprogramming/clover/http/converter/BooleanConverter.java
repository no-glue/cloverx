package org.esmerilprogramming.clover.http.converter;

import org.esmerilprogramming.clover.http.CloverRequest;

/**
 * @author efraimgentil (efraim.gentil@gmail.com)
 */
public class BooleanConverter implements ParameterConverter {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T translate(Class<T> clazz, String parameterName,
			CloverRequest cloverRequest) {
		
		Object attribute = cloverRequest.getAttribute(parameterName);
		
		if(attribute != null){
			return (T) new Boolean( attribute.toString() );
		}
		
		return null;
	}

}