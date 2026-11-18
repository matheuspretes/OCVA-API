package br.cefetmg.ocva.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.cefetmg.ocva.model.Evento;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    // Verifica se já existe outro evento no mesmo dia (data começa com "aaaa-mm-dd")
    boolean existsByDataStartingWithAndIdNot(String dia, Long id);

    // Eventos que usam uma partitura
    List<Evento> findByPartiturasId(Long partituraId);
}
