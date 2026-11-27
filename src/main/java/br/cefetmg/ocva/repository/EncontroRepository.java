package br.cefetmg.ocva.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.cefetmg.ocva.model.Encontro;

public interface EncontroRepository extends JpaRepository<Encontro, Long> {
    // Todos os encontros, do mais antigo para o mais novo
    List<Encontro> findAllByOrderByDataAscHoraAsc();

    // Encontros cujo dia é até a data informada (usado para achar os que já passaram)
    List<Encontro> findByDataLessThanEqual(LocalDate data);

    // Encontros ligados a um evento
    List<Encontro> findByEventoMarcadoId(Long eventoMarcadoId);

    List<Encontro> findByEventoId(Long eventoId);

    boolean existsByTipoId(Long tipoId);
}
