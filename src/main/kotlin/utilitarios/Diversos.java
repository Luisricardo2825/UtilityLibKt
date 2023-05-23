package utilitarios;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Period;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;

public class Diversos {

	public static Timestamp toTimestamp(String data) {
		data = toZeroInt(data);
		if(data.equals("0")) {
			return null;
		}else {
		Timestamp dataf = Timestamp.valueOf(data+" 00:00:00");
		return dataf;
		}
	}
	
	public static String toZeroInt(Object valor) {
		
		if(valor.toString().equals("")){
			return "0";
		}else {
			return valor.toString();
		}
		
	}

	/**
	 * Transforma um GregorianCalendar em uma String com os valores de ano mes e dia
	 * no Formato yyyy-mm-dd
	 *
	 * @param gcrData - GregorianCalendar que se deseja transformar
	 * @return sTransData- String com o formato yyyy-mm-dd
	 */
	public static String strTransData(Date gcrData) {
		String sTransData;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		sTransData = sdf.format(gcrData.getTime());

		return sTransData;
	}

	/**
	 * Transforma um GregorianCalendar em uma String com os valores de ano mes e dia
	 * no Formato yyyy-mm-dd
	 *
	 * @param gcrData - GregorianCalendar que se deseja transformar
	 * @return sTransData- String com o formato yyyy-mm-dd
	 */
	public static String strTransDataYYYYMMDD(Date gcrData) {
		String sTransData;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sTransData = sdf.format(gcrData.getTime());

		return sTransData;
	}

	/**
	 * Transforma um GregorianCalendar em uma String com os valores de ano mes e dia
	 * no Formato yyyy-mm-dd
	 *
	 * @param gcrData - GregorianCalendar que se deseja transformar
	 * @return sTransData- String com o formato yyyy-mm-dd
	 */
	public static String strTransDataCompleta(Date gcrData) {
		String sTransData;
		SimpleDateFormat sdfDia = new SimpleDateFormat("dd");
		SimpleDateFormat sdfMes = new SimpleDateFormat("MMMM");
		SimpleDateFormat sdfAno = new SimpleDateFormat("yyyy");

		sTransData = sdfDia.format(gcrData.getTime()) + " de " + sdfMes.format(gcrData.getTime()) + " de "
				+ sdfAno.format(gcrData.getTime());

		return sTransData;
	}

	/**
	 * Formatar Telefone/Celular O padr�o impresso pela sankhya � 00 0000-0000
	 * apos o DDD n�o inseridos dois espa�os totalizando o tamanho 13
	 */
	public static String converterTelefone(String telefone) {
		if (telefone == null) {
			telefone = "(00) 0000-0000";
		}

		if (telefone.length() == 13) {
			telefone = "(" + telefone.substring(0, 2) + ") " + telefone.substring(4, 8) + "-"
					+ telefone.substring(8, 12);
		}

		return telefone;
	}

	public static String converterCelular(String celular) {
		if (celular == null) {
			celular = "(00) 0000-0000";
		}

		if (celular.length() == 13) {
			celular = "(" + celular.substring(0, 2) + ") " + celular.substring(4, 9) + "-" + celular.substring(9, 13);
		}

		return celular;
	}

	/**
	 * Formatar CNPJ e CPF
	 */
	public static String formatarCnpjCpf(String cnpj_cpf) {
		if (cnpj_cpf.length() == 14) {
			cnpj_cpf = cnpj_cpf.substring(0, 2) + "." + cnpj_cpf.substring(2, 5) + "." + cnpj_cpf.substring(5, 8) + "/"
					+ cnpj_cpf.substring(8, 12) + "-" + cnpj_cpf.substring(12, 14);
		} else {
			cnpj_cpf = cnpj_cpf.substring(0, 3) + "." + cnpj_cpf.substring(3, 6) + "." + cnpj_cpf.substring(6, 9) + "-"
					+ cnpj_cpf.substring(9, 11);
		}

		return cnpj_cpf;
	}

	/**
	 * Formatar CEP
	 */
	public static String formatarCEP(String cep) {
		if (cep.length() == 8) {
			cep = cep.substring(0, 5) + "-" + cep.substring(5);
		}

		return cep;
	}

	/**
	 * Formatar hor�rio de entrada e saida
	 */
	public static String formatarEntradaSaida(String valor) {
		if (valor.length() == 3) {
			valor = "0" + valor.substring(0, 1) + ":" + valor.substring(1, 3); // 0800
		} else {
			valor = valor.substring(0, 2) + ":" + valor.substring(2, 4);
		}
		return valor;
	}

	/**
	 * Calcular Diferenca de datas retornando apenas os meses
	 **/
	public static int encontrarDiferencaDatas(Date dataInicio, Date dataFim) {
		DateTime dtInicio = new DateTime(dataInicio);
		DateTime dtFim = new DateTime(dataFim);

		if (dataFim == null) {
			dtFim = dtInicio.plusMonths(11);
			Period period = new Period(dtInicio, dtFim);
			// period.getMonths();
			int ano = period.getYears() * 12;
			int mes = period.getMonths();
			return mes + ano;
		}

		Period period = new Period(dtInicio, dtFim);
		// period.getMonths();
		int ano = period.getYears() * 12;
		int mes = period.getMonths();
		return mes + ano;

		// mes = mes.substring(1, 3);
		// int mesInt = Integer.parseInt(mes);

	}

	/*
	 * M�todo para valida��o de datas
	 */
	public static void validaData(PersistenceEvent contexto, String campoDataFinal, String campoDataInicial)
			throws MGEModelException {

		DynamicVO cupomVO = (DynamicVO) contexto.getVo();
		Timestamp dtInicial = cupomVO.asTimestamp(campoDataInicial);
		Timestamp dtFim = cupomVO.asTimestamp(campoDataFinal);

		if (dtFim == null) {
			return;
		}

		if (dtInicial.after(dtFim)) {

			try {
				throw new MGEModelException("Data inicial n�o pode ser maior que a data final.");
			} catch (Exception e) {
				MGEModelException.throwMe(e);
			}
		}

	}

}
