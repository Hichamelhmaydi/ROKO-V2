package com.example.roko.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActiviteVoyageSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void migratePivotTable() {
        try {
            jdbcTemplate.execute("""
                DO $$
                BEGIN
                  IF EXISTS (
                    SELECT 1 FROM information_schema.tables
                    WHERE table_name = 'activites_voyages'
                  ) THEN

                    DELETE FROM activites_voyages a
                    USING activites_voyages b
                    WHERE a.ctid < b.ctid
                      AND a.activite_id = b.activite_id
                      AND a.voyage_id = b.voyage_id;

                    ALTER TABLE activites_voyages DROP CONSTRAINT IF EXISTS activites_voyages_pkey;
                    ALTER TABLE activites_voyages DROP COLUMN IF EXISTS id;
                    ALTER TABLE activites_voyages DROP COLUMN IF EXISTS prix;
                    ALTER TABLE activites_voyages DROP COLUMN IF EXISTS obligatoire;
                    ALTER TABLE activites_voyages DROP COLUMN IF EXISTS ordre_affichage;
                    ALTER TABLE activites_voyages DROP COLUMN IF EXISTS jour_prevu;
                    ALTER TABLE activites_voyages DROP COLUMN IF EXISTS duree_minutes;
                    ALTER TABLE activites_voyages DROP COLUMN IF EXISTS notes;
                    ALTER TABLE activites_voyages DROP COLUMN IF EXISTS disponible;

                    ALTER TABLE activites_voyages ALTER COLUMN activite_id SET NOT NULL;
                    ALTER TABLE activites_voyages ALTER COLUMN voyage_id SET NOT NULL;

                    ALTER TABLE activites_voyages
                      ADD CONSTRAINT activites_voyages_pkey PRIMARY KEY (activite_id, voyage_id);
                  END IF;
                END $$;
                """);
            log.info("Migration activites_voyages appliquée: table pivot simplifiée");
        } catch (Exception ex) {
            log.error("Echec migration activites_voyages", ex);
            throw ex;
        }
    }
}
