/**
 * Copyright (c) 2010 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the MIT/X11 license. The text of the license can be
 * found at http://www.opensource.org/licenses/mit-license.php. 
 */

package org.extensiblecatalog.ncip.v2.service;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import java.util.GregorianCalendar;
import java.util.List;
import java.math.BigDecimal;

/**
 * Carries data elements describing the RenewItemResponse.
 */
public class RenewItemResponseData implements NCIPResponseData {

    /**
     * ResponseHeader
     */
    protected org.extensiblecatalog.ncip.v2.service.ResponseHeader responseHeader;

    /**
     * Set ResponseHeader.
     */
    public void setResponseHeader(org.extensiblecatalog.ncip.v2.service.ResponseHeader responseHeader) {

        this.responseHeader = responseHeader;

    }

    /**
     * Get ResponseHeader.
     */
    public org.extensiblecatalog.ncip.v2.service.ResponseHeader getResponseHeader() {

        return responseHeader;

    }

    /**
     * Problems
     */
    protected List<org.extensiblecatalog.ncip.v2.service.Problem> problems;

    /**
     * Set Problems.
     */
    public void setProblems(List<org.extensiblecatalog.ncip.v2.service.Problem> problems) {

        this.problems = problems;

    }

    /**
     * Get Problems.
     */
    public List<org.extensiblecatalog.ncip.v2.service.Problem> getProblems() {

        return problems;

    }

    /**
     * RequiredFeeAmount
     */
    protected org.extensiblecatalog.ncip.v2.service.RequiredFeeAmount requiredFeeAmount;

    /**
     * Set RequiredFeeAmount.
     */
    public void setRequiredFeeAmount(org.extensiblecatalog.ncip.v2.service.RequiredFeeAmount requiredFeeAmount) {

        this.requiredFeeAmount = requiredFeeAmount;

    }

    /**
     * Get RequiredFeeAmount.
     */
    public org.extensiblecatalog.ncip.v2.service.RequiredFeeAmount getRequiredFeeAmount() {

        return requiredFeeAmount;

    }

    /**
     * RequiredItemUseRestrictionTypes
     */
    protected List<org.extensiblecatalog.ncip.v2.service.ItemUseRestrictionType> requiredItemUseRestrictionTypes;

    /**
     * Set RequiredItemUseRestrictionTypes.
     */
    public void setRequiredItemUseRestrictionTypes(
        List<org.extensiblecatalog.ncip.v2.service.ItemUseRestrictionType> requiredItemUseRestrictionTypes) {

        this.requiredItemUseRestrictionTypes = requiredItemUseRestrictionTypes;

    }

    /**
     * Get RequiredItemUseRestrictionTypes.
     */
    public List<org.extensiblecatalog.ncip.v2.service.ItemUseRestrictionType> getRequiredItemUseRestrictionTypes() {

        return requiredItemUseRestrictionTypes;

    }

    /**
     * Pending
     */
    protected org.extensiblecatalog.ncip.v2.service.Pending pending;

    /**
     * Set Pending.
     */
    public void setPending(org.extensiblecatalog.ncip.v2.service.Pending pending) {

        this.pending = pending;

    }

    /**
     * Get Pending.
     */
    public org.extensiblecatalog.ncip.v2.service.Pending getPending() {

        return pending;

    }

    /**
     * ItemId
     */
    protected org.extensiblecatalog.ncip.v2.service.ItemId itemId;

    /**
     * Set ItemId.
     */
    public void setItemId(org.extensiblecatalog.ncip.v2.service.ItemId itemId) {

        this.itemId = itemId;

    }

    /**
     * Get ItemId.
     */
    public org.extensiblecatalog.ncip.v2.service.ItemId getItemId() {

        return itemId;

    }

    /**
     * UserId
     */
    protected org.extensiblecatalog.ncip.v2.service.UserId userId;

    /**
     * Set UserId.
     */
    public void setUserId(org.extensiblecatalog.ncip.v2.service.UserId userId) {

        this.userId = userId;

    }

    /**
     * Get UserId.
     */
    public org.extensiblecatalog.ncip.v2.service.UserId getUserId() {

        return userId;

    }

    /**
     * DateDue
     */
    protected GregorianCalendar dateDue;

    /**
     * Set DateDue.
     */
    public void setDateDue(GregorianCalendar dateDue) {

        this.dateDue = dateDue;

    }

    /**
     * Get DateDue.
     */
    public GregorianCalendar getDateDue() {

        return dateDue;

    }

    /**
     * DateForReturn
     */
    protected GregorianCalendar dateForReturn;

    /**
     * Set DateForReturn.
     */
    public void setDateForReturn(GregorianCalendar dateForReturn) {

        this.dateForReturn = dateForReturn;

    }

    /**
     * Get DateForReturn.
     */
    public GregorianCalendar getDateForReturn() {

        return dateForReturn;

    }

    /**
     * RenewalCount
     */
    protected BigDecimal renewalCount;

    /**
     * Set RenewalCount.
     */
    public void setRenewalCount(BigDecimal renewalCount) {

        this.renewalCount = renewalCount;

    }

    /**
     * Get RenewalCount.
     */
    public BigDecimal getRenewalCount() {

        return renewalCount;

    }

    /**
     * FiscalTransactionInformation
     */
    protected org.extensiblecatalog.ncip.v2.service.FiscalTransactionInformation fiscalTransactionInformation;

    /**
     * Set FiscalTransactionInformation.
     */
    public void setFiscalTransactionInformation(
        org.extensiblecatalog.ncip.v2.service.FiscalTransactionInformation fiscalTransactionInformation) {

        this.fiscalTransactionInformation = fiscalTransactionInformation;

    }

    /**
     * Get FiscalTransactionInformation.
     */
    public org.extensiblecatalog.ncip.v2.service.FiscalTransactionInformation getFiscalTransactionInformation() {

        return fiscalTransactionInformation;

    }

    /**
     * ItemOptionalFields
     */
    protected org.extensiblecatalog.ncip.v2.service.ItemOptionalFields itemOptionalFields;

    /**
     * Set ItemOptionalFields.
     */
    public void setItemOptionalFields(org.extensiblecatalog.ncip.v2.service.ItemOptionalFields itemOptionalFields) {

        this.itemOptionalFields = itemOptionalFields;

    }

    /**
     * Get ItemOptionalFields.
     */
    public org.extensiblecatalog.ncip.v2.service.ItemOptionalFields getItemOptionalFields() {

        return itemOptionalFields;

    }

    /**
     * UserOptionalFields
     */
    protected org.extensiblecatalog.ncip.v2.service.UserOptionalFields userOptionalFields;

    /**
     * Set UserOptionalFields.
     */
    public void setUserOptionalFields(org.extensiblecatalog.ncip.v2.service.UserOptionalFields userOptionalFields) {

        this.userOptionalFields = userOptionalFields;

    }

    /**
     * Get UserOptionalFields.
     */
    public org.extensiblecatalog.ncip.v2.service.UserOptionalFields getUserOptionalFields() {

        return userOptionalFields;

    }

    /**
     * Generic toString() implementation.
     *
     * @return String
     */
    @Override
    public String toString() {

        return ReflectionToStringBuilder.reflectionToString(this);

    }

}

