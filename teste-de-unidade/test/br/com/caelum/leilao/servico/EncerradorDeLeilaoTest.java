package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.LeilaoDaoFalso;
import br.com.caelum.leilao.infra.email.Carteiro;

public class EncerradorDeLeilaoTest {

	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAtras() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
				.naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga)
				.constroi();
		
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		LeilaoDaoFalso daoFalso = mock(LeilaoDaoFalso.class);
		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		
		Carteiro carteiroFalso = mock(Carteiro.class);
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso,
				carteiroFalso);
		encerrador.encerra();
		
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
		assertEquals(2, encerrador.getTotalEncerrados());
	}

	@Test
	public void deveAtualizarLeiloesEncerrados() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
				.naData(antiga).constroi();
		LeilaoDaoFalso daoFalso = mock(LeilaoDaoFalso.class);
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));
		
		Carteiro carteiroFalso = mock(Carteiro.class);
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso,
				carteiroFalso);
		encerrador.encerra();
		
		verify(daoFalso, times(1)).atualiza(leilao1);
	}

	@Test
	public void deveContinuarAExecucaoMesmoQuandoDaoFalha() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
				.naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga)
				.constroi();
		
		LeilaoDaoFalso daoFalso = mock(LeilaoDaoFalso.class);
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
		
		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);
		
		Carteiro carteiroFalso = mock(Carteiro.class);
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso,
				carteiroFalso);
		encerrador.encerra();
		
		verify(daoFalso).atualiza(leilao2);
		verify(carteiroFalso).envia(leilao2);
	}
}