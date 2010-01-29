/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: November 23rd, 2009 jonathan@opennms.org
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.reporting.availability.svclayer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.api.reporting.DeliveryOptions;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportService;
import org.opennms.api.reporting.ReportValidationService;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.dao.OnmsReportConfigDao;
import org.opennms.reporting.availability.AvailabilityCalculationException;
import org.opennms.reporting.availability.AvailabilityCalculator;
import org.opennms.reporting.availability.ReportMailer;
import org.opennms.reporting.availability.render.HTMLReportRenderer;
import org.opennms.reporting.availability.render.PDFReportRenderer;
import org.opennms.reporting.availability.render.ReportRenderException;
import org.opennms.reporting.availability.render.ReportRenderer;
import org.opennms.reporting.core.svclayer.ParameterConversionService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

public class AvailabilityReportService implements ReportService,
        ReportValidationService {

    private AvailabilityCalculator m_classicCalculator;

    private AvailabilityCalculator m_calendarCalculator;

    private OnmsReportConfigDao m_configDao;

    private Category log;

    private ParameterConversionService m_parameterConversionService;

    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    private static final String CAL_TYPE = "calendar";

    private static final String HTML_OUTPUT_FILE_NAME = "AvailReport.html";
    private static final String SVG_OUTPUT_FILE_NAME = "SVGAvailReport.pdf";
    private static final String PDF_OUTPUT_FILE_NAME = "PDFAvailReport.pdf";

    public AvailabilityReportService() {

        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        log = ThreadCategory.getInstance(AvailabilityReportService.class);

    }

    public boolean validate(HashMap<String, Object> reportParms,
            String reportID) {

        if (!reportParms.containsKey("endDate")) {
            log.fatal("report parameters should contain parameter endDate");
            return false;
        }

        if (!(reportParms.get("endDate") instanceof Date)) {
            log.fatal("report parameters 'endDate' should be a Date");
            return false;
        }

        if (!reportParms.containsKey("reportCategory")) {
            log.fatal("report parameters should contain parameter reportCategory");
            return false;
        }

        if (!(reportParms.get("reportCategory") instanceof String)) {
            log.fatal("report parameter 'reportCategory' should be a String");
            return false;
        }

        return true;

    }

    public void run(HashMap<String, Object> reportParms,
            DeliveryOptions deliveryOptions, String reportId) {

        Resource xsltResource;
        String inputFileName;
        String outputFileName;
        ReportRenderer renderer;

        inputFileName = runInternal(reportParms, reportId,
                                    deliveryOptions.getPersist());

        if ((inputFileName != null) && deliveryOptions.getSendMail()) {

            try {

                
                switch (deliveryOptions.getFormat()) {

                case HTML:
                    log.debug("rendering as HTML");
                    renderer = new HTMLReportRenderer();
                    xsltResource = new UrlResource(
                                                   m_configDao.getHtmlStylesheetLocation(reportId));
                    outputFileName = HTML_OUTPUT_FILE_NAME;
                    break;
                case PDF:
                    log.debug("rendering as PDF");
                    renderer = new PDFReportRenderer();
                    xsltResource = new UrlResource(
                                                   m_configDao.getPdfStylesheetLocation(reportId));
                    outputFileName = PDF_OUTPUT_FILE_NAME;
                    break;
                case SVG:
                    log.debug("rendering as PDF with embedded SVG");
                    renderer = new PDFReportRenderer();
                    xsltResource = new UrlResource(
                                                   m_configDao.getSvgStylesheetLocation(reportId));
                    outputFileName = SVG_OUTPUT_FILE_NAME;
                    break;
                default:
                    log.debug("No vaild format found in delivery options. Rendering as HTML");
                    renderer = new HTMLReportRenderer();
                    xsltResource = new UrlResource(
                                               m_configDao.getHtmlStylesheetLocation(reportId));
                    outputFileName = HTML_OUTPUT_FILE_NAME;
                }
                String baseDir = System.getProperty("opennms.report.dir");
                log.debug("render base dir: " + baseDir);
                log.debug("render input file: " + inputFileName);
                log.debug("render output file: " + outputFileName);
                log.debug("render template: " + xsltResource);
                renderer.setBaseDir(baseDir);
                renderer.render(inputFileName, outputFileName, xsltResource);
                ReportMailer mailer = new ReportMailer(
                                                       deliveryOptions.getMailTo(),
                                                       baseDir
                                                               + "/"
                                                               + outputFileName);
                mailer.send();

            } catch (MalformedURLException e) {
                log.fatal("Malformed URL for xslt template");
            } catch (ReportRenderException e) {
                log.fatal("unable to render report");
            } catch (IOException e) {
                log.fatal("unable to mail report");
            }
        }

    }


    public void render(String id, String location, ReportFormat format,
            OutputStream outputStream) {

        Resource xsltResource;
        ReportRenderer renderer;

        log.debug("request to render report type " + id + " at " + location
                + " as " + format);

        try {

            switch (format) {

            case HTML:
                log.debug("rendering as HTML");
                renderer = new HTMLReportRenderer();
                xsltResource = new UrlResource(
                                               m_configDao.getHtmlStylesheetLocation(id));
                break;
            case PDF:
                log.debug("rendering as PDF");
                renderer = new PDFReportRenderer();
                xsltResource = new UrlResource(
                                               m_configDao.getPdfStylesheetLocation(id));
                break;
            case SVG:
                log.debug("rendering as PDF with embedded SVG");
                renderer = new PDFReportRenderer();
                xsltResource = new UrlResource(
                                               m_configDao.getSvgStylesheetLocation(id));
                break;
            default:
                log.debug("rendering as HTML as no valid format found");
                renderer = new HTMLReportRenderer();
                xsltResource = new UrlResource(
                                               m_configDao.getHtmlStylesheetLocation(id));
            }

            String baseDir = System.getProperty("opennms.report.dir");
            renderer.setBaseDir(baseDir);
            renderer.render(location, outputStream, xsltResource);

        } catch (MalformedURLException e) {
            log.fatal("Malformed URL for xslt template");
        } catch (ReportRenderException e) {
            log.fatal("unable to render report");
        }

    }

    public DeliveryOptions getDeliveryOptions(String reportId, String userId) {

        DeliveryOptions options = new DeliveryOptions();

        options.setMailFormat("HTML");
        options.setCanPersist(true);
        options.setPersist(true);
        options.setSendMail(true);

        UserManager userFactory = UserFactory.getInstance();

        try {
            String emailAddress = userFactory.getEmail(userId);
            if (emailAddress != null) {
                options.setMailTo(userFactory.getEmail(userId));
            }
        } catch (MarshalException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ValidationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return options;

    }


    public List<ReportFormat> getFormats(String id) {

        List<ReportFormat> formats = new ArrayList<ReportFormat>();

        if (m_configDao.getHtmlStylesheetLocation(id) != null)
            formats.add(ReportFormat.HTML);
        if (m_configDao.getPdfStylesheetLocation(id) != null)
            formats.add(ReportFormat.PDF);
        if (m_configDao.getSvgStylesheetLocation(id) != null)
            formats.add(ReportFormat.SVG);

        return formats;
    }

    private String runInternal(HashMap<String, Object> reportParms,
            String reportId, Boolean persist) {

        AvailabilityCalculator calculator;
        String reportFileName = null;

        log.debug("running OpenNMS database report " + reportId);

        if (m_configDao.getType(reportId).equalsIgnoreCase(CAL_TYPE)) {
            calculator = m_calendarCalculator;
            log.debug("Calendar report format selected");
        } else {
            calculator = m_classicCalculator;
            log.debug("Classic report format selected");
        }

        calculator.setCategoryName((String) reportParms.get("reportCategory"));

        calculator.setCalendar(new GregorianCalendar());
        calculator.setPeriodEndDate((Date) reportParms.get("endDate"));

        calculator.setLogoURL(m_configDao.getLogo(reportId));

        // have the calculator calculate everything to enable any of the
        // templates to work
        // This has changed since the last version
        // This will have some performance impact.

        calculator.setReportFormat("all");

        log.debug("Starting Availability Report Calculations");
        try {
            calculator.calculate();
            if (persist) {
                reportFileName = calculator.writeLocateableXML(reportId);
            } else {
                reportFileName = calculator.writeXML();
            }
        } catch (AvailabilityCalculationException ce) {
            log.fatal("Unable to calculate report data ", ce);
        }

        return reportFileName;

    }

    
    public ReportParameters getParameters(String ReportId) {
        return m_parameterConversionService.convert(m_configDao.getParameters(ReportId));
    }
    
    public void setCalendarCalculator(AvailabilityCalculator calculator) {
        m_calendarCalculator = calculator;
    }

    public void setClassicCalculator(AvailabilityCalculator calulator) {
        m_classicCalculator = calulator;
    }

    public void setConfigDao(OnmsReportConfigDao configDao) {
        m_configDao = configDao;
    }
    
    public void setParameterConversionService(ParameterConversionService parameterConversionService) {
        m_parameterConversionService = parameterConversionService;
    }

}
