package org.extensiblecatalog.ncip.v2.aleph.restdlf.handlers;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephConstants;
import org.extensiblecatalog.ncip.v2.aleph.restdlf.item.AlephRequestItem;
import org.extensiblecatalog.ncip.v2.aleph.util.AlephUtil;
import org.extensiblecatalog.ncip.v2.service.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AlephRequestHandler extends DefaultHandler {
	private SimpleDateFormat alephDateFormatter;
	private SimpleDateFormat alephHourFormatter;
	private TimeZone localTimeZone;
	private AlephRequestItem requestItem;
	private String itemIdToLookFor;
	private String requestLink;
	private boolean parsingRequest = false;
	private boolean holdRequestFound = false;
	private boolean requestNumberReached = false;
	private boolean hourPlacedReached = false;
	private boolean earliestDateNeededReached = false;
	private boolean needBeforeDateReached = false;
	private boolean datePlacedReached = false;
	private boolean pickupLocationReached = false;
	private boolean pickupExpiryDateReached = false;
	private boolean requestTypeReached = false;
	private boolean pickupDateReached = false;
	private boolean statusReached = false;
	private boolean holdQueueLengthReached = false;

	public AlephRequestHandler(String itemIdToLookFor, AlephRequestItem requestItem) {
		this.itemIdToLookFor = itemIdToLookFor;
		this.requestItem = requestItem;
		alephDateFormatter = new SimpleDateFormat("yyyyMMdd");
		alephHourFormatter = new SimpleDateFormat("HHmm");
		localTimeZone = TimeZone.getTimeZone("ECT");
	}
	
	public AlephRequestHandler setParsingRequests() {
		parsingRequest = true;
		return this;
	}
/*		responseData.setRequestScopeType(requestItem.getRequestScopeType());
		responseData.setRequestType(requestItem.getRequestType());
		responseData.setRequestId(requestItem.getRequestId());
		responseData.setItemOptionalFields(requestItem.getItemOptionalFields());
		responseData.setUserOptionalFields(requestItem.getUserOptionalFields()); 

		// Not implemented services, most of them probably even not implementable
		responseData.setDateAvailable(requestItem.getDateAvailable());
		responseData.setHoldQueuePosition(requestItem.getHoldQueuePosition());
		responseData.setShippingInformation(requestItem.getShippingInformation());
		responseData.setAcknowledgedFeeAmount(requestItem.getAcknowledgedFeeAmout());
		responseData.setDateOfUserRequest(requestItem.getDateOfUserRequest());
		responseData.setEarliestDateNeeded(requestItem.getEarliestDateNeeded());
		responseData.setHoldQueuePosition(requestItem.getHoldQueuePosition());
		responseData.setNeedBeforeDate(requestItem.getNeedBeforeDate());
		responseData.setPaidFeeAmount(requestItem.getPaidFeeAmount());
		responseData.setPickupDate(requestItem.getHoldPickupDate());
		responseData.setPickupExpiryDate(requestItem.getPickupExpiryDate());
		responseData.setPickupLocation(requestItem.getPickupLocation());
		responseData.setRequestStatusType(requestItem.getRequestStatusType());*/
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (parsingRequest) {
			if (qName.equalsIgnoreCase(AlephConstants.Z37_REQUEST_NUMBER_NODE)) {
				requestNumberReached = true;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_OPEN_DATE_NODE)) {
				datePlacedReached = true;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_OPEN_HOUR_NODE)) {
				hourPlacedReached = true;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_REQUEST_DATE_NODE)) {
				earliestDateNeededReached = true;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_END_REQUEST_DATE_NODE)) {
				needBeforeDateReached = true;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_HOLD_SEQUENCE_NODE)) {
				holdQueueLengthReached = true;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_PICKUP_LOCATION_NODE)) {
				pickupLocationReached = true;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_END_HOLD_DATE_NODE)) {
				pickupExpiryDateReached = true;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_PRIORITY_NODE)) {
				requestTypeReached = true;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_HOLD_DATE_NODE)) {
				pickupDateReached = true;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_STATUS_NODE)) {
				statusReached = true;
			}
		} else {
			if (qName.equalsIgnoreCase(AlephConstants.HOLD_REQUEST_NODE)) {
				String link = attributes.getValue(AlephConstants.HREF_NODE_ATTR);
				if (link.indexOf(itemIdToLookFor) > -1) {
					requestLink = link;
					holdRequestFound = true;
				}
			}
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (parsingRequest) {
			if (qName.equalsIgnoreCase(AlephConstants.Z37_REQUEST_NUMBER_NODE) && requestNumberReached) {
				requestNumberReached = false;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_OPEN_DATE_NODE) && datePlacedReached) {
				datePlacedReached = false;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_OPEN_HOUR_NODE) && hourPlacedReached) {
				hourPlacedReached = false;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_REQUEST_DATE_NODE) && earliestDateNeededReached) {
				earliestDateNeededReached = false;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_END_REQUEST_DATE_NODE) && needBeforeDateReached) {
				needBeforeDateReached = false;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_HOLD_SEQUENCE_NODE) && holdQueueLengthReached) {
				holdQueueLengthReached = false;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_PICKUP_LOCATION_NODE) && pickupLocationReached) {
				pickupLocationReached = false;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_END_HOLD_DATE_NODE) && pickupExpiryDateReached) {
				pickupExpiryDateReached = false;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_PRIORITY_NODE) && requestTypeReached) {
				requestTypeReached = false;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_HOLD_DATE_NODE) && pickupDateReached) {
				pickupDateReached = false;
			} else if (qName.equalsIgnoreCase(AlephConstants.Z37_STATUS_NODE) && statusReached) {
				statusReached = false;
			}
		}
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		if (parsingRequest) {
			if (requestNumberReached) {
				String requestIdParsed = new String(ch, start, length);
				RequestId requestId = new RequestId();
				requestId.setRequestIdentifierValue(requestIdParsed);
				requestItem.setRequestId(requestId);
				requestNumberReached = false;
			} else if (datePlacedReached) {
				String datePlacedParsed = new String(ch, start, length);
				if (!datePlacedParsed.equalsIgnoreCase("00000000")) {
					GregorianCalendar datePlaced = new GregorianCalendar(localTimeZone);

					try {
						datePlaced.setTime(alephDateFormatter.parse(datePlacedParsed));
						if (AlephUtil.inDaylightTime())
							datePlaced.add(Calendar.HOUR_OF_DAY, 2);
					} catch (ParseException e) {
						e.printStackTrace();
					}

					requestItem.setDateOfUserRequest(datePlaced);
				}
				datePlacedReached = false;
			} else if (hourPlacedReached) {
				String hourPlacedParsed = new String(ch, start, length);
				if (!hourPlacedParsed.equalsIgnoreCase("00000000")) {
					GregorianCalendar datePlaced = requestItem.getDateOfUserRequest();
					GregorianCalendar hourPlaced = new GregorianCalendar(localTimeZone);

					try {
						hourPlaced.setTime(alephHourFormatter.parse(hourPlacedParsed));
					} catch (ParseException e) {
						e.printStackTrace();
					}

					datePlaced.add(Calendar.HOUR_OF_DAY, hourPlaced.get(Calendar.HOUR_OF_DAY));
					datePlaced.add(Calendar.MINUTE, hourPlaced.get(Calendar.MINUTE));

					requestItem.setDateOfUserRequest(datePlaced);
				}
				hourPlacedReached = false;
			} else if (earliestDateNeededReached) {
				String earliestDateNeededParsed = new String(ch, start, length);
				if (!earliestDateNeededParsed.equalsIgnoreCase("00000000")) {
					GregorianCalendar earliestDateNeeded = new GregorianCalendar(localTimeZone);

					try {
						earliestDateNeeded.setTime(alephDateFormatter.parse(earliestDateNeededParsed));
						if (AlephUtil.inDaylightTime())
							earliestDateNeeded.add(Calendar.HOUR_OF_DAY, 2);
					} catch (ParseException e) {
						e.printStackTrace();
					}

					requestItem.setEarliestDateNeeded(earliestDateNeeded);
				}
				earliestDateNeededReached = false;
			} else if (needBeforeDateReached) {
				String needBeforeDateParsed = new String(ch, start, length);
				if (!needBeforeDateParsed.equalsIgnoreCase("00000000")) {
					GregorianCalendar needBeforeDate = new GregorianCalendar(localTimeZone);

					try {
						needBeforeDate.setTime(alephDateFormatter.parse(needBeforeDateParsed));
						if (AlephUtil.inDaylightTime())
							needBeforeDate.add(Calendar.HOUR_OF_DAY, 2);
					} catch (ParseException e) {
						e.printStackTrace();
					}

					requestItem.setNeedBeforeDate(needBeforeDate);
				}
				needBeforeDateReached = false;
			} else if (holdQueueLengthReached) {
				requestItem.setHoldQueueLength(new BigDecimal(new String(ch, start, length)));
				holdQueueLengthReached = false;
			} else if (pickupLocationReached) {
				PickupLocation pickupLocation = new PickupLocation(new String(ch, start, length));
				requestItem.setPickupLocation(pickupLocation);
				pickupLocationReached = false;
			} else if (pickupExpiryDateReached) {
				String pickupExpiryDateParsed = new String(ch, start, length);
				if (!pickupExpiryDateParsed.equalsIgnoreCase("00000000")) {
					GregorianCalendar pickupExpiryDate = new GregorianCalendar(localTimeZone);

					try {
						pickupExpiryDate.setTime(alephDateFormatter.parse(pickupExpiryDateParsed));
						if (AlephUtil.inDaylightTime())
							pickupExpiryDate.add(Calendar.HOUR_OF_DAY, 2);
					} catch (ParseException e) {
						e.printStackTrace();
					}

					requestItem.setPickupExpiryDate(pickupExpiryDate);
				}
				pickupExpiryDateReached = false;
			} else if (requestTypeReached) {
				RequestType requestType = null;
				String parsedValue = new String(ch, start, length);
				if (parsedValue == "30") // TODO: Add remaining request types
					requestType = Version1RequestType.LOAN;
				else
					requestType = Version1RequestType.ESTIMATE; // FIXME: Put here better default value
				requestItem.setRequestType(requestType);
				requestTypeReached = false;
			} else if (pickupDateReached) {
				String pickupDateParsed = new String(ch, start, length);
				if (!pickupDateParsed.equalsIgnoreCase("00000000")) {
					GregorianCalendar pickupDate = new GregorianCalendar(localTimeZone);

					try {
						pickupDate.setTime(alephDateFormatter.parse(pickupDateParsed));
						if (AlephUtil.inDaylightTime())
							pickupDate.add(Calendar.HOUR_OF_DAY, 2);
					} catch (ParseException e) {
						e.printStackTrace();
					}

					requestItem.setHoldPickupDate(pickupDate);
				}
				pickupDateReached = false;
			} else if (statusReached) {
				String parsedStatus = new String(ch, start, length);
				RequestStatusType requestStatusType;
				if (parsedStatus == "S")
					requestStatusType = Version1RequestStatusType.AVAILABLE_FOR_PICKUP;
				else
					requestStatusType = Version1RequestStatusType.IN_PROCESS;
				requestItem.setRequestStatusType(requestStatusType);
				statusReached = false;
			}

		}
	}

	public String getRequestLink() {
		return requestLink;
	}

	public boolean requestWasFound() {
		return holdRequestFound;
	}
}