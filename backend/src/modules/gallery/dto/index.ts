import { IGalleryUpload } from '@shared/interfaces/gallery';

export class GalleryUploadDto implements IGalleryUpload {
  serviceId: string;
  branchId?: string;
}
