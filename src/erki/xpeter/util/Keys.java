/*
 * © Copyright 2008–2010 by Edgar Kalkowski <eMail@edgar-kalkowski.de>
 * 
 * This file is part of the chatbot xpeter.
 * 
 * The chatbot xpeter is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package erki.xpeter.util;

import java.util.LinkedList;
import java.util.TreeMap;

import erki.xpeter.parsers.rss.FeedData;
import erki.xpeter.parsers.rss.RssFeed;
import erki.xpeter.parsers.sms.ShortMessage;
import erki.xpeter.parsers.sms.SimpleMailbox;
import erki.xpeter.parsers.soccer.Soccer;

/**
 * This enum contains constants that can be used to store information in the persistent storage
 * file. A parser should not use the storage keys of other parsers but rather create its own.
 * 
 * @author Edgar Kalkowski
 */
public enum Keys {
    
    /**
     * A {@link TreeMap}&lt;{@link String}, {@link LinkedList}&lt;{@link ShortMessage}&gt;&gt; that
     * contains the short messages stored by {@link SimpleMailbox}.
     */
    SHORT_MESSAGES,

    /**
     * This {@link TreeMap}&lt;{@link String}, {@link LinkedList}&lt;{@link FeedData}&gt;&gt;
     * contains all the feed urls known to {@link RssFeed} together with additional information
     * about the feeds.
     */
    RSS_FEEDS,

    /**
     * This {@link LinkedList}&lt;{@link String}&gt; contains the full urls of soccer games to
     * follow (see {@link Soccer} for details).
     */
    SOCCER_THREADS,
}
