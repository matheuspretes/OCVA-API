package br.cefetmg.ocva.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.cefetmg.ocva.model.EventoMarcado;

public interface EventoMarcadoRepository extends JpaRepository<EventoMarcado, Long> {
    // Todos os eventos marcados, do mais próximo para o mais distante
    List<EventoMarcado> findAllByOrderByDataAscHoraAsc();

    // Eventos cujo dia é até a data informada (usado para achar os que já passaram)
    List<EventoMarcado> findByDataLessThanEqual(LocalDate data);
}
