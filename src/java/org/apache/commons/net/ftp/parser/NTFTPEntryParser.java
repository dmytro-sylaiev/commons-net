/*
 * Copyright 2001-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.ftp.parser;
import java.util.Calendar;
import org.apache.commons.net.ftp.FTPFile;

/**
 * Implementation of FTPFileEntryParser and FTPFileListParser for NT Systems.
 *
 * @author  <a href="Winston.Ojeda@qg.com">Winston Ojeda</a>
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id: NTFTPEntryParser.java,v 1.17 2004/07/28 05:01:47 dfs Exp $
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 */
public class NTFTPEntryParser extends RegexFTPFileEntryParserImpl
{
    /**
     * this is the regular expression used by this parser.
     */
    private static final String REGEX =
        "((?:0[1-9])|(?:1[0-2]))-"
        + "((?:0[1-9])|(?:[1-2]\\d)|(?:3[0-1]))-"
        + "(\\d\\d)\\s*"
        + "((?:0[1-9])|(?:1[012])):"
        + "([0-5]\\d)\\s*"
        + "([AP])M\\s*"
        + "(<DIR>)?\\s*"
        + "([0-9]+)?\\s+"
        + "(\\S.*)";

    /**
     * The sole constructor for an NTFTPEntryParser object.
     *
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen
     * under normal conditions.  It it is seen, this is a sign that
     * <code>REGEX</code> is  not a valid regular expression.
     */
    public NTFTPEntryParser()
    {
        super(REGEX);
    }


    /**
     * Parses a line of an NT FTP server file listing and converts it into a
     * usable format in the form of an <code> FTPFile </code> instance.  If the
     * file listing line doesn't describe a file, <code> null </code> is
     * returned, otherwise a <code> FTPFile </code> instance representing the
     * files in the directory is returned.
     * <p>
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    public FTPFile parseFTPEntry(String entry)
    {
        FTPFile f = new FTPFile();
        f.setRawListing(entry);

        if (matches(entry))
        {
            String mo = group(1);
            String da = group(2);
            String yr = group(3);
            String hr = group(4);
            String min = group(5);
            String ampm = group(6);
            String dirString = group(7);
            String size = group(8);
            String name = group(9);
            if (null == name || name.equals(".") || name.equals(".."))
            {
                return (null);
            }
            f.setName(name);
            //convert all the calendar stuff to ints
            int month = new Integer(mo).intValue() - 1;
            int day = new Integer(da).intValue();
            int year = new Integer(yr).intValue() + 2000;
            int hour = new Integer(hr).intValue();
            int minutes = new Integer(min).intValue();

            // Y2K stuff? this will break again in 2080 but I will
            // be sooooo dead anyways who cares.
            // SMC - IS NT's directory date REALLY still not Y2K-compliant?
            if (year > 2080)
            {
                year -= 100;
            }

            Calendar cal = Calendar.getInstance();
            cal.clear();

            //set the calendar
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DATE, day);
            cal.set(Calendar.MONTH, month);
            int ap = Calendar.AM;
            if ("P".equals(ampm))
            {
                ap = Calendar.PM;
                if (hour != 12) {
                    hour += 12;
                }
            } else if (hour == 12) {
                hour = 0;
            }

            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, minutes);

            // Using Calendar.HOUR_OF_DAY instead of Calendar.HOUR
            // since the latter has proven to be unreliable.
            // see bug 27085

            //          cal.set(Calendar.AM_PM, ap);
            cal.set(Calendar.HOUR_OF_DAY, hour);

            cal.getTime().getTime();
            f.setTimestamp(cal);

            if ("<DIR>".equals(dirString))
            {
                f.setType(FTPFile.DIRECTORY_TYPE);
                f.setSize(0);
            }
            else
            {
                f.setType(FTPFile.FILE_TYPE);
                if (null != size)
                {
                  f.setSize(Long.parseLong(size));
                }
            }
            return (f);
        }
        return null;
    }
}
