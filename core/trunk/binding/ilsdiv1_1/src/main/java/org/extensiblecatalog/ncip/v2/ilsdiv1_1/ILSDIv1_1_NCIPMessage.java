/**
 * Copyright (c) 2010 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the MIT/X11 license. The text of the license can be
 * found at http://www.opensource.org/licenses/mit-license.php.
 */

package org.extensiblecatalog.ncip.v2.ilsdiv1_1;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.extensiblecatalog.ncip.v2.service.*;

public class ILSDIv1_1_NCIPMessage extends NCIPMessage {

	protected ILSDIvOneOneLookupItemSetInitiationData ilsdivOneOnelookupItemSet;
	
	protected ILSDIvOneOneLookupUserInitiationData ilsdivOneOnelookupUser;
	
	protected ILSDIvOneOneLookupUserResponseData ilsdivOneOnelookupUserResponse;

	protected ILSDIvOneOneLookupAgencyResponseData ilsdivOneOnelookupAgencyResponse;

	public void setLookupItemSet(
			ILSDIvOneOneLookupItemSetInitiationData lookupItemSet) {
		this.ilsdivOneOnelookupItemSet = lookupItemSet;
	}

	public ILSDIvOneOneLookupItemSetInitiationData getLookupItemSet() {
		return ilsdivOneOnelookupItemSet;
	}

	public void setLookupUser(
			ILSDIvOneOneLookupUserInitiationData lookupUser) {
		this.ilsdivOneOnelookupUser = lookupUser;
	}

	public ILSDIvOneOneLookupUserInitiationData getLookupUser() {
		return ilsdivOneOnelookupUser;
	}
	
	public void setILSDIvOneOneLookupUserResponse(ILSDIvOneOneLookupUserResponseData ilsdivOneOnelookupUserResponse) {
		this.ilsdivOneOnelookupUserResponse = ilsdivOneOnelookupUserResponse;
	}

    public ILSDIvOneOneLookupUserResponseData getLookupUserResponse() {
        return ilsdivOneOnelookupUserResponse;
    }
	
	public void setILSDIvOneOneLookupAgencyResponse(ILSDIvOneOneLookupAgencyResponseData ilsdivOneOnelookupAgencyResponse) {
		this.ilsdivOneOnelookupAgencyResponse = ilsdivOneOnelookupAgencyResponse;
	}

    public ILSDIvOneOneLookupAgencyResponseData getLookupAgencyResponse() {
        return ilsdivOneOnelookupAgencyResponse;
    }
	
	@Override
	public NCIPInitiationData getInitiationData()
			throws InvocationTargetException, IllegalAccessException,
			ServiceException {

		NCIPInitiationData initData;
		NCIPData ncipData = unwrap(this);
		if (ncipData instanceof NCIPInitiationData) {
			initData = (NCIPInitiationData) ncipData;
		} else {
			throw new ServiceException(ServiceError.INVALID_MESSAGE_FORMAT,
					"Initiation message not a recognized type. (Found '"
							+ ncipData.getClass().getSimpleName() + "'.)");
		}

		return initData;

	}

	@Override
    public NCIPResponseData getResponseData() throws InvocationTargetException, IllegalAccessException, ServiceException {

        NCIPResponseData respData;
        NCIPData ncipData = unwrap(this);
        if ( ncipData instanceof NCIPResponseData ) {
            respData = (NCIPResponseData) ncipData;
        } else {
            throw new ServiceException(ServiceError.INVALID_MESSAGE_FORMAT,
                    "Response message not a recognized type. (Found '" + ncipData.getClass().getSimpleName() + "'.)");
        }

        return respData;

    }

	/**
	 * Iterate over the fields, find the first {@Link NCIPData} field
	 * with a getter with a non-null result and return it.
	 *
	 * @param ncipMessage
	 * @return
	 */
	public NCIPData unwrap(NCIPMessage ncipMessage)
			throws IllegalAccessException, InvocationTargetException {

		NCIPData result = null;

		if (ncipMessage != null) {

			Class<? extends NCIPMessage> objClass = ncipMessage.getClass();
			Class<?>superClass = objClass.getSuperclass();

			Object[] fields1 = objClass.getDeclaredFields();
			Object[] fields2 = superClass.getDeclaredFields();

			Object[] fields = ArrayUtils.addAll(fields1, fields2);

			for (Object field : fields) {

				if (field instanceof Field) {
					
					Field f = (Field) field;

					String toSearch = "get" + f.getName();
					
					Object obj = null;

					if (toSearch.equalsIgnoreCase("getilsdivOneOnelookupItemSet")) {
						obj = super.getLookupItemSet();
					} else if (toSearch.equalsIgnoreCase("getilsdivOneOnelookupUser")) {
						obj = super.getLookupUser();
					}
					
					if (obj != null) {
						result = (NCIPData) obj;
						break;
					}

					Method m = ReflectionHelper.findMethod(superClass, toSearch);

					if (m != null) {

						obj = m.invoke(ncipMessage);
						if (obj != null
								&& NCIPData.class.isAssignableFrom(obj
										.getClass())) {

							result = (NCIPData) obj;
							break;

						}

					}
				}

			}

		}

		return result;

	}
}
