package br.com.nfmanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Data
@Getter
@Setter
@With
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ClientClient {
    @Id
    private int id;
    private int idPrestador;
    private int idTomador;
    private String codServ;
    private String descServ;
    private String NBS;
}
