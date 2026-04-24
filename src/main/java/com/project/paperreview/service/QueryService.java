// === QueryService.java ===
package com.project.paperreview.service;

import com.project.paperreview.entity.Query;
import com.project.paperreview.repository.QueryRepository;
import com.project.paperreview.socket.QueryAlertClient;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService {

    @Autowired
    private QueryRepository queryRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private QueryAlertClient queryAlertClient;    // NEW: inject socket client

    // ── ADD QUERY ─────────────────────────────────────────────────

    public Query addQuery(Query query) {
        query.setStatus("Pending");
        return queryRepository.save(query);
    }

    // ── RESOLVE QUERY ─────────────────────────────────────────────

    /**
     * @Transactional: if anything fails, database changes are rolled back.
     *
     * Steps:
     *  1. Find query by ID
     *  2. Set status to "Resolved"
     *  3. Save to database
     *  4. Send @Async notification (runs in background thread — CO1)
     *  5. Send socket alert (CO4)
     */
    @Transactional
    public Query resolveQuery(Long id) {

        Query q = queryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Query not found: id=" + id));

        q.setStatus("Resolved");
        Query saved = queryRepository.save(q);

        // CO1: @Async — runs on a separate thread, does not block HTTP response
        notificationService.sendResolutionAlert(q.getPrn(), q.getQuestion());

        // CO4: Socket — sends TCP alert to QueryAlertServer (also @Async)
        queryAlertClient.sendResolvedAlert(q.getPrn(), q.getQuestion());

        return saved;
    }

    // ── QUERIES ───────────────────────────────────────────────────

    public long countPendingQueries()       { return queryRepository.countByStatus("Pending"); }
    public List<Query> getAllQueries()       { return queryRepository.findAll(); }
    public List<Query> getPendingQueries()  { return queryRepository.findByStatus("Pending"); }
    public List<Query> getQueriesByPrn(String prn) { return queryRepository.findByPrn(prn); }

    // ── SCHEDULED TASK (CO1) ──────────────────────────────────────

    @Scheduled(fixedRate = 60000)
    public void checkPendingQueries() {
        long count = queryRepository.countByStatus("Pending");
        System.out.println("[SCHEDULER] Pending queries: " + count);
    }
}