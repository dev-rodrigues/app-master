package com.httpsantos.cursomc.services;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.httpsantos.cursomc.domain.Categoria;
import com.httpsantos.cursomc.dto.CategoriaDTO;
import com.httpsantos.cursomc.repositories.CategoriaRepository;
import com.httpsantos.cursomc.services.exceptions.DataIntegrityViolationsException;
import com.httpsantos.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class CategoriaService {

	@Autowired
	private CategoriaRepository repo;

	// pesquisar uma categoria
	public Categoria find(Integer id) {
		Optional<Categoria> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Categoria.class.getName()));
	}
	
	@Transactional
	// inserir uma categoria
	public Categoria insert(Categoria obj) {
		obj.setId(null);
		return repo.save(obj);
	}
	
	@Transactional
	// atualizar uma categoria
	public Categoria update(Categoria obj) {
		Categoria newObj = find(obj.getId());
		updateData(newObj, obj);
		return repo.save(newObj);
	}
	
	@Transactional
	private void updateData(Categoria newObj, Categoria obj) {
		newObj.setNome(obj.getNome());
	}
	
	@Transactional
	// apagar uma categoria
	public void delete(Integer id) {
		find(id);
		try {
			repo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityViolationsException("Não é possivel excluir uma Categoria que possui produtos!");
		}
	}

	// listar todas as categorias
	public List<Categoria> findAll() {
		return repo.findAll();
	}

	// listar categoria com paginacao
	public Page<Categoria> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}

	// METODO AUXILIAR QUE INSTANCIA UMA CATEGORIA APARTIR DE UMA CATEGORIA DTO
	public Categoria fromDTO(CategoriaDTO objDTO) {
		return new Categoria(objDTO.getId(), objDTO.getNome());
	}
}