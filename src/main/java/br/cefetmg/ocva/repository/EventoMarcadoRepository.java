package br.cefetmg.ocva.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.cefetmg.ocva.model.EventoMarcado;

public interface EventoMarcadoRepository extends JpaRepository<EventoMarcado, Long> {
    // Eventos de hoje em diante, do mais próximo para o mais distante
    List<EventoMarcado> findByDataGreaterThanEqualOrderByDataAscHoraAsc(LocalDate data);

    // Eventos cujo dia já passou (serão convertidos em eventos realizados)
    List<EventoMarcado> findByDataBefore(LocalDate data);
}
