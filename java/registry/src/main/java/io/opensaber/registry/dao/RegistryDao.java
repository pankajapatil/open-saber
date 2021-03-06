package io.opensaber.registry.dao;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import io.opensaber.registry.exception.AuditFailedException;
import org.apache.tinkerpop.gremlin.structure.Graph;
import io.opensaber.registry.exception.DuplicateRecordException;
import io.opensaber.registry.exception.EncryptionException;
import io.opensaber.registry.exception.RecordNotFoundException;

public interface RegistryDao {

	public List getEntityList();

	public String addEntity(Graph entity, String label) throws DuplicateRecordException, EncryptionException, AuditFailedException, RecordNotFoundException;

	public boolean updateEntity(Graph entityForUpdate, String rootNodeLabel, String methodOrigin)
			throws RecordNotFoundException, NoSuchElementException, EncryptionException, AuditFailedException;

	public boolean deleteEntity(String rootLabel, String labelToBeDeleted) throws RecordNotFoundException,AuditFailedException;

	public Graph getEntityById(String label) throws RecordNotFoundException, NoSuchElementException, EncryptionException, AuditFailedException;

}
