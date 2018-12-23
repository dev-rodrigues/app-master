package com.httpsantos.cursomc.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.httpsantos.cursomc.domain.Cidade;
import com.httpsantos.cursomc.repositories.CidadeRepository;

@Service
public class CidadeService {

	@Autowired
	private CidadeRepository cidadeRepository;

	public List<Cidade> findByEstado(Integer estadoId) {
		return cidadeRepository.findCidades(estadoId);
	}
}
