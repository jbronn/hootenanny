/*
 * This file is part of Hootenanny.
 *
 * Hootenanny is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * --------------------------------------------------------------------
 *
 * The following copyright notices are generated automatically. If you
 * have a new notice to add, please use the format:
 * " * @copyright Copyright ..."
 * This will properly maintain the copyright information. DigitalGlobe
 * copyrights will be updated automatically.
 *
 * @copyright Copyright (C) 2016 DigitalGlobe (http://www.digitalglobe.com/)
 */
package hoot.services.readers.review;

import static hoot.services.models.db.QReviewBookmarks.reviewBookmarks;

import java.sql.Connection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.sql.SQLQuery;

import hoot.services.models.db.ReviewBookmarks;
import hoot.services.utils.DbUtils;


public class ReviewBookmarkRetriever {
    private static final Logger logger = LoggerFactory.getLogger(ReviewBookmarkRetriever.class);

    private final Connection conn;

    public ReviewBookmarkRetriever(Connection cn) {
        this.conn = cn;
    }

    public List<ReviewBookmarks> retrieve(long mapId, long relationId) {
        SQLQuery<ReviewBookmarks> query = this.getQuery(mapId, relationId);
        List<ReviewBookmarks> reviewBookmarks = query.fetch();
        return reviewBookmarks;
    }

    public List<ReviewBookmarks> retrieve(long boookMarkId) {
        SQLQuery<ReviewBookmarks> query = this.getQuery(boookMarkId);
        List<ReviewBookmarks> reviewBookmarks = query.fetch();
        return reviewBookmarks;
    }

    /**
     * Retrieves all review tags
     *
     * @param orderByCol
     *            - order by column to sort
     * @param isAsc
     *            - is order by asc | desc
     * @param limit
     *            - limit for numbers of returned results
     * @param offset
     *            - offset row for paging
     * @return - list of Review tags
     */
    public List<ReviewBookmarks> retrieveAll(String orderByCol, boolean isAsc, long limit,
            long offset, Long[] creatorArray, Long[] layerArray) {
        SQLQuery<ReviewBookmarks> query = this.getAllQuery(orderByCol, isAsc, limit, offset, creatorArray, layerArray);
        List<ReviewBookmarks> reviewBookmarks = query.fetch();
        return reviewBookmarks;
    }

    /**
     * Get the total counts of all review tags
     * 
     * @return - numbers of toal count
     */
    public long getBookmarksCount() {
        long count = new SQLQuery(this.conn, DbUtils.getConfiguration())
                .from(reviewBookmarks)
                .fetchCount();
        return count;
    }

    /**
     * SQL Query for retrieving review tag
     * 
     * @param mapId
     * @param relationId
     * @return - SQLQuery
     */
    private SQLQuery<ReviewBookmarks> getQuery(long mapId, long relationId) {
        SQLQuery<ReviewBookmarks> query =
                new SQLQuery<>(this.conn, DbUtils.getConfiguration())
                        .select(reviewBookmarks)
                        .from(reviewBookmarks)
                        .where(reviewBookmarks.mapId.eq(mapId)
                                .and(reviewBookmarks.relationId.eq(relationId)));
        return query;
    }

    private SQLQuery<ReviewBookmarks> getQuery(long bookmarkId) {
        SQLQuery<ReviewBookmarks> query =
                new SQLQuery<>(this.conn, DbUtils.getConfiguration())
                        .select(reviewBookmarks)
                        .from(reviewBookmarks)
                        .where(reviewBookmarks.id.eq(bookmarkId));
        return query;
    }

    /**
     * SQL Query for retrieving all tags
     */
    private SQLQuery<ReviewBookmarks> getAllQuery(String orderByCol, boolean isAsc, long limit, long offset,
            Long[] creatorArray, Long[] layerArray) {

        SQLQuery<ReviewBookmarks> query = new SQLQuery<>(this.conn, DbUtils.getConfiguration());
        if ((creatorArray != null) && (layerArray != null)) {
            query.select(reviewBookmarks)
                    .where(reviewBookmarks.createdBy.in((Number[]) creatorArray)
                            .and(reviewBookmarks.mapId.in((Number[]) layerArray)))
                    .orderBy(getSpecifier(orderByCol, isAsc));
        }
        else if ((creatorArray != null) && (layerArray == null)) {
            query.from(reviewBookmarks)
                    .where(reviewBookmarks.createdBy.in((Number[]) creatorArray))
                    .orderBy(getSpecifier(orderByCol, isAsc));
        }
        else if ((creatorArray == null) && (layerArray != null)) {
            query.from(reviewBookmarks)
                    .where(reviewBookmarks.mapId.in((Number[]) layerArray))
                    .orderBy(getSpecifier(orderByCol, isAsc));
        }
        else {
            query.from(reviewBookmarks).orderBy(getSpecifier(orderByCol, isAsc));
        }

        if (limit > -1) {
            query.limit(limit);
        }

        if (offset > -1) {
            query.offset(offset);
        }

        return query;
    }

    /**
     * Filter for allowed columns for order by
     * 
     * @param orderByCol
     *            - String representation of order by column
     * @param isAsc
     *            - asc | dsc
     * @return - OrderSpecifier
     */
    private OrderSpecifier<?> getSpecifier(String orderByCol, boolean isAsc) {
        OrderSpecifier<?> order = reviewBookmarks.id.asc();
        if (orderByCol != null) {
            switch (orderByCol) {
                case "id":
                    order = (isAsc) ? reviewBookmarks.id.asc() : reviewBookmarks.id.desc();
                break;
                case "createdAt":
                    order = (isAsc) ? reviewBookmarks.createdAt.asc() : reviewBookmarks.createdAt.desc();
                break;
                case "createdBy":
                    order = (isAsc) ? reviewBookmarks.createdBy.asc() : reviewBookmarks.createdBy.desc();
                break;
                case "lastModifiedAt":
                    order = (isAsc) ? reviewBookmarks.lastModifiedAt.asc() : reviewBookmarks.lastModifiedAt.desc();
                break;
                case "lastModifiedBy":
                    order = (isAsc) ? reviewBookmarks.lastModifiedBy.asc() : reviewBookmarks.lastModifiedBy.desc();
                break;
                case "mapId":
                    order = (isAsc) ? reviewBookmarks.mapId.asc() : reviewBookmarks.mapId.desc();
                break;
                case "relationId":
                    order = (isAsc) ? reviewBookmarks.relationId.asc() : reviewBookmarks.relationId.desc();
                break;
                default:
                    order = reviewBookmarks.id.asc();
                break;
            }
        }

        return order;
    }
}
