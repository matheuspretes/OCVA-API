package br.cefetmg.ocva.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.cefetmg.ocva.model.Encontro;

public interface EncontroRepository extends JpaRepository<Encontro, Long> {
    // Encontros de hoje em diante, do mais próximo para o mais distante
    List<Encontro> findByDataGreaterThanEqualOrderByDataAscHoraAsc(LocalDate data);

    // Encontros cujo dia já passou (serão convertidos em ensaios)
    List<Encontro> findByDataBefore(LocalDate data);

    boolean existsByTipoId(Long tipoId);
}
