package br.cefetmg.ocva.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.cefetmg.ocva.model.TipoEncontro;

public interface TipoEncontroRepository extends JpaRepository<TipoEncontro, Long> {
    boolean existsByNomeIgnoreCase(String nome);

    List<TipoEncontro> findAllByOrderByIdAsc();
}
