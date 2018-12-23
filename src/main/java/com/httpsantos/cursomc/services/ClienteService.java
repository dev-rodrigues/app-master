package com.httpsantos.cursomc.services;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.httpsantos.cursomc.domain.Cidade;
import com.httpsantos.cursomc.domain.Cliente;
import com.httpsantos.cursomc.domain.Endereco;
import com.httpsantos.cursomc.domain.enums.Perfil;
import com.httpsantos.cursomc.domain.enums.TipoCliente;
import com.httpsantos.cursomc.dto.ClienteDTO;
import com.httpsantos.cursomc.dto.ClienteNewDTO;
import com.httpsantos.cursomc.repositories.ClienteRepository;
import com.httpsantos.cursomc.repositories.EnderecoRepository;
import com.httpsantos.cursomc.security.UserSS;
import com.httpsantos.cursomc.services.exceptions.AuthorizationException;
import com.httpsantos.cursomc.services.exceptions.DataIntegrityViolationsException;
import com.httpsantos.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {

	@Value("${img.prefix.client.profile}")
	private String prefixo;

	@Value("${img.profile.size}")
	private Integer size;

	@Autowired
	private BCryptPasswordEncoder enconder;

	@Autowired
	private ClienteRepository repo;
//
//	@Autowired
//	private ClienteService clienteService;
	@Autowired
	private EnderecoRepository end;

	@Autowired
	private S3Service s3Service;

	@Autowired
	private ImageService imageService;

	public Cliente find(Integer id) {

		UserSS userSS = UserService.authenticated();
		if (userSS == null || !userSS.hasRole(Perfil.ADMIN) && !id.equals(userSS.getId())) {
			throw new AuthorizationException("Acesso negado");
		}

		Optional<Cliente> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName()));
	}

	@Transactional
	// inserir uma cliente
	public Cliente insert(Cliente obj) {
		obj.setId(null);
		obj = repo.save(obj);
		end.saveAll(obj.getEnderecos());
		return obj;
	}

	@Transactional
	// atualizar uma cliente
	public Cliente update(Cliente obj) {
		Cliente newObj = find(obj.getId());
		updateData(newObj, obj);
		return repo.save(newObj);
	}

	@Transactional
	private void updateData(Cliente newObj, Cliente obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());
	}

	@Transactional
	// apagar uma cliente
	public void delet(Integer id) {
		find(id);
		try {
			repo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityViolationsException("Não é possivel excluir porque há entidades relacionadas!");
		}
	}

	// listar todas as cliente
	public List<Cliente> findAll() {
		return repo.findAll();
	}

	public Cliente findByEmail(String email) {
		UserSS user = UserService.authenticated();
		if (user == null || !user.hasRole(Perfil.ADMIN) && !email.equals(user.getUsername())) {
			throw new AuthorizationException("Acesso negado.");
		}

		Cliente obj = repo.findByEmail(email);
		if (obj == null) {
			throw new ObjectNotFoundException(
					"Objeto não encontrado! Id: " + user.getId() + ", Tipo: " + Cliente.class.getName());
		}
		return obj;
	}

	// listar cliente com paginacao
	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}

	// METODO AUXILIAR QUE INSTANCIA UMA cliente APARTIR DE UMA clienteDTO
	public Cliente fromDTO(ClienteDTO objDTO) {
		return new Cliente(objDTO.getId(), objDTO.getNome(), objDTO.getEmail(), null, null, null);
	}

	public Cliente fromDTO(ClienteNewDTO objDto) {
		Cliente cli = new Cliente(null, objDto.getNome(), objDto.getEmail(), objDto.getCpfOuCnpj(),
				TipoCliente.toEnum(objDto.getTipo()), enconder.encode(objDto.getSenha()));
		Cidade cid = new Cidade(objDto.getCidadeId(), null, null);
		Endereco end = new Endereco(null, objDto.getLogradouro(), objDto.getNumero(), objDto.getComplemento(),
				objDto.getBairro(), objDto.getCep(), cli, cid);
		cli.getEnderecos().add(end);
		cli.getTelefones().add(objDto.getTelefone1());
		if (objDto.getTelefone2() != null) {
			cli.getTelefones().add(objDto.getTelefone2());
		}
		if (objDto.getTelefone3() != null) {
			cli.getTelefones().add(objDto.getTelefone3());
		}
		return cli;
	}

	public URI uploadProfilePicture(MultipartFile multipartFile) {
		UserSS user = UserService.authenticated();
		if (user == null) {
			throw new AuthorizationException("Acesso negado.");
		}
		BufferedImage jpgImage = imageService.getJpgImageFromFile(multipartFile);
		jpgImage = imageService.cropSquare(jpgImage);
		jpgImage = imageService.resize(jpgImage, size);
		String fileName = prefixo + user.getId() + ".jpg";

		return s3Service.uploadFile(imageService.getInputStream(jpgImage, "jpg"), fileName, "image");

//		URI uri = s3Service.uploadFile(multipartFile);
//		Cliente cli = clienteService.find(user.getId());
////		cli.setImageURL(uri.toString());
//		repo.save(cli);
//		return uri;
	}
}
