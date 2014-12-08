/*
 * Copyright (C) 2014 the original author or authors.
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
package ro.fortsoft.pippo.pebble;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ro.fortsoft.pippo.core.PippoRuntimeException;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.template.EvaluationContext;

public class FormatTimeExtension extends AbstractExtension {

    public FormatTimeExtension() {
    }

    @Override
    public Map<String, Filter> getFilters() {
        Map<String, Filter> filters = new HashMap<>();
        filters.put("formatTime", new FormatTimeFilter());
        return filters;
    }

    public class FormatTimeFilter implements Filter {

        @Override
        public List<String> getArgumentNames() {
            List<String> names = new ArrayList<>();
            names.add("format");
            names.add("existingFormat");
            return names;
        }

        @Override
        public Object apply(Object input, Map<String, Object> args) {
            if (input == null) {
                return null;
            }

            EvaluationContext context = (EvaluationContext) args.get("_context");
            Locale locale = context.getLocale();

            DateFormat existingFormat = null;
            DateFormat intendedFormat = null;

            String format = (String) args.get("format");
            int type = parseStyle(format);
            if (type == -1) {
                intendedFormat = new SimpleDateFormat(format, locale);
            } else {
                intendedFormat = DateFormat.getDateTimeInstance(type, type, locale);
            }

            Date date;
            if (args.get("existingFormat") != null) {
                existingFormat = new SimpleDateFormat((String) args.get("existingFormat"), locale);
                try {
                    date = existingFormat.parse((String) input);
                } catch (ParseException e) {
                    throw new RuntimeException("Could not parse date", e);
                }
            } else {
                date = getDateObject(input);
            }

            return intendedFormat.format(date);
        }

        private Date getDateObject(Object value) {

            if (value instanceof Date) {
                return (Date) value;
            } else if (value instanceof Calendar) {
                return ((Calendar) value).getTime();
            } else if (value instanceof Long) {
                return new Date((Long) value);
            } else {
                throw new PippoRuntimeException("Failed to get a date object from {}!", value);
            }
        }

        protected Integer parseStyle(String style) {
            if ("full".equals(style)) {
                return DateFormat.FULL;
            } else if ("long".equals(style)) {
                return DateFormat.LONG;
            } else if ("short".equals(style)) {
                return DateFormat.SHORT;
            } else if ("medium".equals(style)) {
                return DateFormat.MEDIUM;
            } else {
                return -1;
            }
        }
    }
}
